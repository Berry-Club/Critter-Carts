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
import net.minecraft.world.level.saveddata.SavedData
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