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
		val nearbyLines = lines.values.filter { line -> chunkPos in getChunkPositions(line) }
		if (nearbyLines.isEmpty()) return

		AddWebLinesPacket(nearbyLines).messagePlayer(player)
	}

	fun removeInvalidLines(level: ServerLevel) {
		val validityCache: MutableMap<UUID, Boolean> = mutableMapOf()
		val invalidLines = lines.values.filter { line ->
			isLoaded(level, line) && !isValid(level, line, mutableSetOf(), validityCache)
		}
		if (invalidLines.isEmpty()) return

		for (line in invalidLines) {
			lines.remove(line.uuid)
			sendToNearbyPlayers(level, line, RemoveWebLinePacket(line.uuid))
		}

		setDirty()
	}

	private fun isLoaded(level: ServerLevel, line: WebLine): Boolean {
		val visitedLines: MutableSet<UUID> = mutableSetOf()
		return isNodeLoaded(level, line.firstNode, visitedLines)
			&& isNodeLoaded(level, line.secondNode, visitedLines)
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

	private fun isValid(
		level: ServerLevel,
		line: WebLine,
		visitedLines: MutableSet<UUID>,
		validityCache: MutableMap<UUID, Boolean>
	): Boolean {
		val cachedValidity = validityCache[line.uuid]
		if (cachedValidity != null) return cachedValidity
		if (!visitedLines.add(line.uuid)) return false

		val dependenciesValid = isNodeValid(level, line.firstNode, visitedLines, validityCache)
			&& isNodeValid(level, line.secondNode, visitedLines, validityCache)
		if (!dependenciesValid) {
			visitedLines.remove(line.uuid)
			validityCache[line.uuid] = false
			return false
		}

		val lineValid = hasLineOfSight(level, line.firstNode.position, line.secondNode.position)

		visitedLines.remove(line.uuid)
		validityCache[line.uuid] = lineValid
		return lineValid
	}

	private fun hasLineOfSight(level: ServerLevel, firstPosition: Vec3, secondPosition: Vec3): Boolean {
		val clipContext = ClipContext(
			firstPosition,
			secondPosition,
			ClipContext.Block.COLLIDER,
			ClipContext.Fluid.NONE,
			CollisionContext.empty()
		)

		return level.clip(clipContext).type == HitResult.Type.MISS
	}

	private fun isNodeValid(
		level: ServerLevel,
		node: WebNode,
		visitedLines: MutableSet<UUID>,
		validityCache: MutableMap<UUID, Boolean>
	): Boolean {
		if (!node.isValid(level, lines::containsKey)) return false
		if (node !is WebNode.LineAnchor) return true

		val anchoredLine = lines[node.lineUuid] ?: return false
		return isValid(level, anchoredLine, visitedLines, validityCache)
	}

	private fun sendToNearbyPlayers(level: ServerLevel, line: WebLine, packet: AaronPacket) {
		val nearbyPlayers: MutableSet<ServerPlayer> = mutableSetOf()

		for (chunkPos in getChunkPositions(line)) {
			for (player in level.chunkSource.chunkMap.getPlayers(chunkPos, false)) {
				nearbyPlayers.add(player)
			}
		}

		for (player in nearbyPlayers) {
			packet.messagePlayer(player)
		}
	}

	private fun getChunkPositions(line: WebLine): Set<ChunkPos> {
		val chunkPositions: MutableSet<ChunkPos> = mutableSetOf()
		val visitedLines: MutableSet<UUID> = mutableSetOf()
		addNodeChunkPosition(line.firstNode, chunkPositions, visitedLines)
		addNodeChunkPosition(line.secondNode, chunkPositions, visitedLines)
		return chunkPositions
	}

	private fun addNodeChunkPosition(
		node: WebNode,
		chunkPositions: MutableSet<ChunkPos>,
		visitedLines: MutableSet<UUID>
	) {
		when (node) {
			is WebNode.BlockAnchor -> chunkPositions.add(ChunkPos(node.blockPos))
			is WebNode.LineAnchor -> {
				if (!visitedLines.add(node.lineUuid)) return

				val anchoredLine = lines[node.lineUuid] ?: return
				addNodeChunkPosition(anchoredLine.firstNode, chunkPositions, visitedLines)
				addNodeChunkPosition(anchoredLine.secondNode, chunkPositions, visitedLines)
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

			return level.dataStorage.computeIfAbsent(FACTORY, SAVED_DATA_NAME)
		}

		private val FACTORY = Factory(::WebSavedData, ::load)
	}
}