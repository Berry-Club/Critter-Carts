package dev.aaronhowser.mods.critter_carts.handler.web.node

import com.mojang.serialization.Codec
import dev.aaronhowser.mods.critter_carts.handler.web.WebLine
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.phys.Vec3
import java.util.UUID

sealed interface WebNode {

	val uuid: UUID
	val position: Vec3

	fun isLoaded(level: ServerLevel): Boolean
	fun isValid(level: ServerLevel, lines: Map<UUID, WebLine>, checkedLines: MutableSet<UUID>): Boolean

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