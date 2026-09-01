package dev.aaronhowser.mods.critter_carts.handler.web

import com.mojang.serialization.DynamicOps
import dev.aaronhowser.mods.aaron.packet.AaronPacket
import dev.aaronhowser.mods.critter_carts.handler.web.node.WebNode
import dev.aaronhowser.mods.critter_carts.packet.server_to_client.AddWebLinesPacket
import dev.aaronhowser.mods.critter_carts.packet.server_to_client.RemoveWebLinePacket
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.saveddata.SavedData
import java.util.UUID

class WebSavedData : SavedData() {

	private val nodes: MutableMap<UUID, WebNode> = mutableMapOf()
	private val lines: MutableMap<UUID, WebLine> = mutableMapOf()

	private val lineUuidsByChunk: MutableMap<ChunkPos, MutableSet<UUID>> = mutableMapOf()
	private val chunksToValidate: MutableSet<ChunkPos> = mutableSetOf()

	fun addLine(level: ServerLevel, line: WebLine) {
		nodes[line.firstNode.uuid] = line.firstNode
		nodes[line.secondNode.uuid] = line.secondNode

		val previousLine = lines.put(line.uuid, line)
		if (previousLine != null) {
			removeLineReferences(previousLine)
		}

		addToChunkCache(line)
		setDirty()
		sendToNearbyPlayers(level, line, AddWebLinesPacket.fromLines(listOf(line)))
	}

	fun getNode(uuid: UUID): WebNode? {
		return nodes[uuid]
	}

	fun getCanonicalNode(node: WebNode): WebNode {
		return nodes[node.uuid] ?: node
	}

	fun getLine(uuid: UUID): WebLine? {
		return lines[uuid]
	}

	fun removeLine(level: ServerLevel, uuid: UUID): WebLine? {
		val removedLine = lines[uuid] ?: return null
		removeStoredLine(level, removedLine)
		chunksToValidate.addAll(removedLine.intersectedChunkPositions)
		setDirty()
		return removedLine
	}

	fun syncChunk(player: ServerPlayer, chunkPos: ChunkPos) {
		val nearbyLines = lines.values.filter { line -> chunkPos in line.getEndpointChunkPositions() }
		if (nearbyLines.isEmpty()) return

		AddWebLinesPacket.fromLines(nearbyLines).messagePlayer(player)
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

	fun markForValidation(blockPos: BlockPos) {
		chunksToValidate.add(ChunkPos(blockPos))
	}

	fun validateChangedChunks(level: ServerLevel) {
		if (chunksToValidate.isEmpty()) return

		val lineUuids = getLinesNearChangedChunks()
		val invalidLines = getInvalidLines(level, lineUuids)
		if (invalidLines.isEmpty()) return

		val obstructingBlockPositions = removeInvalidLines(level, invalidLines)
		playObstructionSounds(level, obstructingBlockPositions)
		setDirty()
	}

	private fun getLinesNearChangedChunks(): Set<UUID> {
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
		return lineUuids
	}

	private fun getInvalidLines(
		level: ServerLevel,
		lineUuids: Set<UUID>
	): Map<WebLine, WebLineInvalidation> {
		val invalidLines: MutableMap<WebLine, WebLineInvalidation> = mutableMapOf()
		for (lineUuid in lineUuids) {
			val line = lines[lineUuid] ?: continue
			if (!line.isLoaded(level)) continue

			val invalidation = line.getInvalidation(level, lines) ?: continue
			invalidLines[line] = invalidation
		}

		return invalidLines
	}

	private fun removeInvalidLines(
		level: ServerLevel,
		invalidLines: Map<WebLine, WebLineInvalidation>
	): Set<BlockPos> {
		val obstructingBlockPositions: MutableSet<BlockPos> = mutableSetOf()
		for ((line, invalidation) in invalidLines) {
			removeStoredLine(level, line)

			if (invalidation.reason == WebLineInvalidationReason.OBSTRUCTED) {
				val blockPos = invalidation.blockPos ?: continue
				obstructingBlockPositions.add(blockPos)
			}
		}

		return obstructingBlockPositions
	}

	private fun removeStoredLine(level: ServerLevel, line: WebLine) {
		lines.remove(line.uuid)
		removeLineReferences(line)
		sendToNearbyPlayers(level, line, RemoveWebLinePacket(line.uuid))
	}

	private fun removeLineReferences(line: WebLine) {
		removeFromChunkCache(line)
		removeNodeIfOrphaned(line.firstNode.uuid)
		removeNodeIfOrphaned(line.secondNode.uuid)
	}

	private fun playObstructionSounds(
		level: ServerLevel,
		blockPositions: Set<BlockPos>
	) {
		for (blockPos in blockPositions) {
			level.playSound(
				null,
				blockPos,
				SoundEvents.ARROW_SHOOT,
				SoundSource.BLOCKS,
				0.5f,
				2f
			)
		}
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

	private fun removeNodeIfOrphaned(nodeUuid: UUID) {
		for (line in lines.values) {
			if (line.firstNode.uuid == nodeUuid || line.secondNode.uuid == nodeUuid) return
		}

		nodes.remove(nodeUuid)
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

		val nodesTag = WebNode.CODEC
			.listOf()
			.encodeStart(ops, nodes.values.toList())
			.getOrThrow()

		val linesTag = WebLineData.CODEC
			.listOf()
			.encodeStart(ops, lines.values.map(WebLine::data))
			.getOrThrow()

		tag.put(NODES_TAG, nodesTag)
		tag.put(LINES_TAG, linesTag)
		return tag
	}

	companion object {
		private const val SAVED_DATA_NAME = "critter_carts_webs"
		private const val NODES_TAG = "Nodes"
		private const val LINES_TAG = "Lines"

		private fun load(tag: CompoundTag, registries: HolderLookup.Provider): WebSavedData {
			val savedData = WebSavedData()
			val nodesTag = tag.get(NODES_TAG) ?: return savedData
			val linesTag = tag.get(LINES_TAG) ?: return savedData
			val ops: DynamicOps<Tag> = registries.createSerializationContext(NbtOps.INSTANCE)
			val loadedNodes = WebNode.CODEC
				.listOf()
				.parse(ops, nodesTag)
				.getOrThrow()
			val loadedLines = WebLineData.CODEC
				.listOf()
				.parse(ops, linesTag)
				.getOrThrow()

			for (node in loadedNodes) {
				savedData.nodes[node.uuid] = node
			}

			for (lineData in loadedLines) {
				val firstNode = savedData.nodes[lineData.firstNodeUuid] ?: continue
				val secondNode = savedData.nodes[lineData.secondNodeUuid] ?: continue
				val line = WebLine(lineData.uuid, firstNode, secondNode)
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