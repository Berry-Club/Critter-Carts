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
						is WebBlockAnchor -> WebBlockAnchor.TYPE
						is WebLineAnchor -> WebLineAnchor.TYPE
					}
				},
				{ type ->
					when (type) {
						WebBlockAnchor.TYPE -> WebBlockAnchor.CODEC
						WebLineAnchor.TYPE -> WebLineAnchor.CODEC
						else -> error("Unknown web node type: $type")
					}
				}
			)

		val STREAM_CODEC: StreamCodec<ByteBuf, WebNode> =
			ByteBufCodecs.VAR_INT.dispatch(
				{ node ->
					when (node) {
						is WebBlockAnchor -> WebBlockAnchor.TYPE_ID
						is WebLineAnchor -> WebLineAnchor.TYPE_ID
					}
				},
				{ typeId ->
					when (typeId) {
						WebBlockAnchor.TYPE_ID -> WebBlockAnchor.STREAM_CODEC
						WebLineAnchor.TYPE_ID -> WebLineAnchor.STREAM_CODEC
						else -> error("Unknown web node type: $typeId")
					}
				}
			)
	}

}