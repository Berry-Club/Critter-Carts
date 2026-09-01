package dev.aaronhowser.mods.critter_carts.handler.web

import com.mojang.serialization.DynamicOps
import dev.aaronhowser.mods.aaron.packet.AaronPacket
import dev.aaronhowser.mods.critter_carts.packet.server_to_client.AddWebLinesPacket
import dev.aaronhowser.mods.critter_carts.packet.server_to_client.RemoveWebLinePacket
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import java.util.UUID

class WebSavedData : SavedData() {
	private val lines: MutableMap<UUID, WebLine> = mutableMapOf()

	fun getLines(): Collection<WebLine> {
		return lines.values
	}

	fun getLine(uuid: UUID): WebLine? {
		return lines[uuid]
	}

	fun addLine(level: ServerLevel, line: WebLine) {
		lines[line.uuid] = line
		setDirty()
		sendToNearbyPlayers(level, line, AddWebLinesPacket(listOf(line)))
	}

	fun removeLine(level: ServerLevel, uuid: UUID): WebLine? {
		val removedLine = lines.remove(uuid) ?: return null
		setDirty()
		sendToNearbyPlayers(level, removedLine, RemoveWebLinePacket(uuid))
		return removedLine
	}

	fun syncChunk(player: ServerPlayer, chunkPos: ChunkPos) {
		val nearbyLines = lines.values.filter { line -> line.getChunkPositions().contains(chunkPos) }
		if (nearbyLines.isEmpty()) return

		AddWebLinesPacket(nearbyLines).messagePlayer(player)
	}

	fun removeInvalidLines(level: ServerLevel) {
		var removedLine = true

		while (removedLine) {
			removedLine = false
			val lineIterator = lines.values.iterator()

			while (lineIterator.hasNext()) {
				val line = lineIterator.next()
				if (!isLoaded(level, line)) continue
				if (isValid(level, line)) continue

				lineIterator.remove()
				setDirty()
				sendToNearbyPlayers(level, line, RemoveWebLinePacket(line.uuid))
				removedLine = true
			}
		}
	}

	private fun isLoaded(level: ServerLevel, line: WebLine): Boolean {
		for (node in listOf(line.firstNode, line.secondNode)) {
			if (!isNodeLoaded(level, node, mutableSetOf())) return false
		}

		return true
	}

	private fun isNodeLoaded(level: ServerLevel, node: WebNode, visitedLines: MutableSet<UUID>): Boolean {
		return when (node) {
			is WebNode.BlockAnchor -> level.hasChunk(node.blockPos.x shr 4, node.blockPos.z shr 4)
			is WebNode.LineAnchor -> {
				if (!visitedLines.add(node.lineUuid)) return true

				val anchoredLine = lines[node.lineUuid] ?: return true
				isNodeLoaded(level, anchoredLine.firstNode, visitedLines)
					&& isNodeLoaded(level, anchoredLine.secondNode, visitedLines)
			}
		}
	}

	private fun isValid(level: ServerLevel, line: WebLine): Boolean {
		val firstPosition = getNodePosition(level, line.firstNode, mutableSetOf()) ?: return false
		val secondPosition = getNodePosition(level, line.secondNode, mutableSetOf()) ?: return false
		val clipContext = ClipContext(
			firstPosition,
			secondPosition,
			ClipContext.Block.COLLIDER,
			ClipContext.Fluid.NONE,
			CollisionContext.empty()
		)

		return level.clip(clipContext).type == HitResult.Type.MISS
	}

	private fun getNodePosition(level: ServerLevel, node: WebNode, visitedLines: MutableSet<UUID>): Vec3? {
		return when (node) {
			is WebNode.BlockAnchor -> {
				val blockState = level.getBlockState(node.blockPos)
				if (!blockState.isFaceSturdy(level, node.blockPos, node.face)) return null

				val blockCenter = Vec3.atCenterOf(node.blockPos)
				val faceOffset = Vec3.atLowerCornerOf(node.face.normal)
					.scale(0.5 + SURFACE_OFFSET)
				blockCenter.add(faceOffset)
			}

			is WebNode.LineAnchor -> {
				if (!visitedLines.add(node.lineUuid)) return null

				val anchoredLine = lines[node.lineUuid] ?: return null
				val firstPosition = getNodePosition(level, anchoredLine.firstNode, visitedLines) ?: return null
				val secondPosition = getNodePosition(level, anchoredLine.secondNode, visitedLines) ?: return null
				firstPosition.lerp(secondPosition, node.percentAlong)
			}
		}
	}

	private fun sendToNearbyPlayers(level: ServerLevel, line: WebLine, packet: AaronPacket) {
		val chunkPositions = line.getChunkPositions()
		for (player in level.players()) {
			if (chunkPositions.any { chunkPos -> level.chunkSource.chunkMap.getPlayers(chunkPos, false).contains(player) }) {
				packet.messagePlayer(player)
			}
		}
	}

	override fun save(tag: CompoundTag, registries: HolderLookup.Provider): CompoundTag {
		val ops = registries.createSerializationContext(NbtOps.INSTANCE)
		val linesTag = WebLine.CODEC
			.listOf()
			.encodeStart(ops, lines.values.toList())
			.getOrThrow()

		tag.put(LINES_TAG, linesTag)
		return tag
	}

	companion object {
		const val SAVED_DATA_NAME = "critter_carts_webs"
		const val LINES_TAG = "Lines"
		private const val SURFACE_OFFSET = 0.001

		private fun load(tag: CompoundTag, registries: HolderLookup.Provider): WebSavedData {
			val savedData = WebSavedData()
			val linesTag = tag.get(LINES_TAG) ?: return savedData
			val ops: DynamicOps<Tag> = registries.createSerializationContext(NbtOps.INSTANCE)
			val loadedLines = WebLine.CODEC
				.listOf()
				.parse(ops, linesTag)
				.getOrThrow()

			for (line in loadedLines) {
				savedData.lines[line.uuid] = line
			}

			return savedData
		}

		fun get(level: ServerLevel): WebSavedData {
			if (level != level.server.overworld()) {
				return get(level.server.overworld())
			}

			val storage = level.dataStorage
			val factory = Factory(::WebSavedData, ::load)
			return storage.computeIfAbsent(factory, SAVED_DATA_NAME)
		}
	}
}