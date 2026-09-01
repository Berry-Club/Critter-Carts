package dev.aaronhowser.mods.critter_carts.handler.web

import com.mojang.serialization.DynamicOps
import dev.aaronhowser.mods.aaron.packet.AaronPacket
import dev.aaronhowser.mods.critter_carts.packet.server_to_client.AddWebLinesPacket
import dev.aaronhowser.mods.critter_carts.packet.server_to_client.RemoveWebLinePacket
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.saveddata.SavedData
import java.util.UUID

class WebSavedData : SavedData() {
	private val lines: MutableMap<UUID, WebLine> = mutableMapOf()
	private val lineUuidsByChunk: MutableMap<ChunkPos, MutableSet<UUID>> = mutableMapOf()
	private val chunksToValidate: MutableSet<ChunkPos> = mutableSetOf()

	fun addLine(level: ServerLevel, line: WebLine) {
		val previousLine = lines.put(line.uuid, line)
		if (previousLine != null) removeFromChunkCache(previousLine)

		addToChunkCache(line)
		setDirty()
		sendToNearbyPlayers(level, line, AddWebLinesPacket(listOf(line)))
	}

	fun getLine(uuid: UUID): WebLine? {
		return lines[uuid]
	}

	fun removeLine(level: ServerLevel, uuid: UUID): WebLine? {
		val removedLine = lines.remove(uuid) ?: return null
		removeFromChunkCache(removedLine)
		setDirty()
		sendToNearbyPlayers(level, removedLine, RemoveWebLinePacket(uuid))
		return removedLine
	}

	fun syncChunk(player: ServerPlayer, chunkPos: ChunkPos) {
		val nearbyLines = lines.values.filter { line -> chunkPos in line.getEndpointChunkPositions() }
		if (nearbyLines.isEmpty()) return

		AddWebLinesPacket(nearbyLines).messagePlayer(player)
	}

	fun forgetChunk(player: ServerPlayer, chunkPos: ChunkPos) {
		val level = player.serverLevel()

		for (line in lines.values) {
			val lineChunks = line.getEndpointChunkPositions()
			if (chunkPos !in lineChunks) continue

			var stillTrackingLine = false
			for (lineChunk in lineChunks) {
				if (lineChunk == chunkPos) continue

				val trackingPlayers = level.chunkSource.chunkMap.getPlayers(lineChunk, false)
				if (player !in trackingPlayers) continue

				stillTrackingLine = true
				break
			}

			if (!stillTrackingLine) {
				RemoveWebLinePacket(line.uuid).messagePlayer(player)
			}
		}
	}

	fun markChunkForValidation(blockPos: BlockPos) {
		chunksToValidate.add(ChunkPos(blockPos))
	}

	fun markChunkForValidation(chunkPos: ChunkPos) {
		chunksToValidate.add(chunkPos)
	}

	fun validateChangedChunks(level: ServerLevel) {
		if (chunksToValidate.isEmpty()) return

		val lineUuids: MutableSet<UUID> = mutableSetOf()
		for (changedChunk in chunksToValidate) {
			for (chunkX in changedChunk.x - 1..changedChunk.x + 1) {
				for (chunkZ in changedChunk.z - 1..changedChunk.z + 1) {
					val chunkPos = ChunkPos(chunkX, chunkZ)
					val cachedUuids = lineUuidsByChunk[chunkPos] ?: continue
					lineUuids.addAll(cachedUuids)
				}
			}
		}

		chunksToValidate.clear()

		val invalidLines = lineUuids.mapNotNull { uuid -> lines[uuid] }.filter { line ->
			line.isLoaded(level) && !line.isValid(level, lines)
		}
		if (invalidLines.isEmpty()) return

		for (line in invalidLines) {
			lines.remove(line.uuid)
			removeFromChunkCache(line)
			sendToNearbyPlayers(level, line, RemoveWebLinePacket(line.uuid))
		}

		setDirty()
	}

	private fun addToChunkCache(line: WebLine) {
		for (chunkPos in line.intersectedChunkPositions) {
			var lineUuids = lineUuidsByChunk[chunkPos]
			if (lineUuids == null) {
				lineUuids = mutableSetOf()
				lineUuidsByChunk[chunkPos] = lineUuids
			}

			lineUuids.add(line.uuid)
		}
	}

	private fun removeFromChunkCache(line: WebLine) {
		for (chunkPos in line.intersectedChunkPositions) {
			val lineUuids = lineUuidsByChunk[chunkPos] ?: continue
			lineUuids.remove(line.uuid)

			if (lineUuids.isEmpty()) {
				lineUuidsByChunk.remove(chunkPos)
			}
		}
	}

	private fun sendToNearbyPlayers(level: ServerLevel, line: WebLine, packet: AaronPacket) {
		val nearbyPlayers: MutableSet<ServerPlayer> = mutableSetOf()

		for (chunkPos in line.getEndpointChunkPositions()) {
			for (player in level.chunkSource.chunkMap.getPlayers(chunkPos, false)) {
				nearbyPlayers.add(player)
			}
		}

		for (player in nearbyPlayers) {
			packet.messagePlayer(player)
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
		private const val SAVED_DATA_NAME = "critter_carts_webs"
		private const val LINES_TAG = "Lines"

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
				savedData.addToChunkCache(line)
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