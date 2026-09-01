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
		val invalidLines = lines.values.filter { line ->
			isLoaded(level, line) && !isValid(level, line, mutableSetOf())
		}
		if (invalidLines.isEmpty()) return

		for (line in invalidLines) {
			lines.remove(line.uuid)
			sendToNearbyPlayers(level, line, RemoveWebLinePacket(line.uuid))
		}

		setDirty()
	}

	private fun isLoaded(level: ServerLevel, line: WebLine): Boolean {
		val checkedLines: MutableSet<UUID> = mutableSetOf()
		return line.firstNode.isLoaded(level, lines, checkedLines)
			&& line.secondNode.isLoaded(level, lines, checkedLines)
	}

	private fun isValid(
		level: ServerLevel,
		line: WebLine,
		checkedLines: MutableSet<UUID>
	): Boolean {
		if (!checkedLines.add(line.uuid)) return false

		if (!isValid(level, line.firstNode, checkedLines)) return false
		if (!isValid(level, line.secondNode, checkedLines)) return false

		checkedLines.remove(line.uuid)
		return hasLineOfSight(level, line.firstNode.position, line.secondNode.position)
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

	private fun isValid(
		level: ServerLevel,
		node: WebNode,
		checkedLines: MutableSet<UUID>
	): Boolean {
		if (!node.isValid(level, lines)) return false
		if (node !is WebNode.LineAnchor) return true

		val line = lines[node.lineUuid] ?: return false
		return isValid(level, line, checkedLines)
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
		return setOf(
			ChunkPos(BlockPos.containing(line.firstNode.position)),
			ChunkPos(BlockPos.containing(line.secondNode.position))
		)
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