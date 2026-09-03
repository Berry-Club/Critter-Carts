package dev.aaronhowser.mods.critter_carts.handler.web.node

import com.mojang.serialization.Codec
import dev.aaronhowser.mods.critter_carts.handler.web.line.WebLine
import dev.aaronhowser.mods.critter_carts.handler.web.line.WebLineInvalidation
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.phys.Vec3
import java.util.*

sealed class WebNode {

	abstract val uuid: UUID
	abstract val position: Vec3

	val lines: Set<WebLine>
		field = mutableSetOf()

	abstract fun isLoaded(level: ServerLevel): Boolean
	abstract fun getInvalidation(
		level: ServerLevel,
		lines: Map<UUID, WebLine>,
		checkedLines: MutableSet<UUID>
	): WebLineInvalidation?

	internal fun addLine(line: WebLine) {
		lines.add(line)
	}

	internal fun removeLine(line: WebLine) {
		lines.remove(line)
	}

	companion object {
		fun isChunkLoaded(level: ServerLevel, chunkPos: ChunkPos): Boolean {
			return level.hasChunk(chunkPos.x, chunkPos.z)
		}

		val CODEC: Codec<WebNode> =
			Codec.STRING.dispatch(
				"type",
				{ node ->
					when (node) {
						is BlockAnchor -> BlockAnchor.TYPE
						is LineAnchor -> LineAnchor.TYPE
					}
				},
				{ type ->
					when (type) {
						BlockAnchor.TYPE -> BlockAnchor.CODEC
						LineAnchor.TYPE -> LineAnchor.CODEC
						else -> error("Unknown web node type: $type")
					}
				}
			)

		val STREAM_CODEC: StreamCodec<ByteBuf, WebNode> =
			ByteBufCodecs.VAR_INT.dispatch(
				{ node ->
					when (node) {
						is BlockAnchor -> BlockAnchor.TYPE_ID
						is LineAnchor -> LineAnchor.TYPE_ID
					}
				},
				{ typeId ->
					when (typeId) {
						BlockAnchor.TYPE_ID -> BlockAnchor.STREAM_CODEC
						LineAnchor.TYPE_ID -> LineAnchor.STREAM_CODEC
						else -> error("Unknown web node type: $typeId")
					}
				}
			)
	}

}