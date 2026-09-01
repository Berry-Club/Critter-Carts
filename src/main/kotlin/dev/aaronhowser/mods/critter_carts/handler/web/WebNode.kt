package dev.aaronhowser.mods.critter_carts.handler.web

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.UUIDUtil
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import java.util.UUID

sealed interface WebNode {
	data class BlockAnchor(
		val blockPos: BlockPos,
		val face: Direction
	) : WebNode {
		companion object {
			const val TYPE = "block"
			const val TYPE_ID = 0

			val CODEC: MapCodec<BlockAnchor> =
				RecordCodecBuilder.mapCodec { instance ->
					instance.group(
						BlockPos.CODEC
							.fieldOf("block_pos")
							.forGetter(BlockAnchor::blockPos),
						Direction.CODEC
							.fieldOf("face")
							.forGetter(BlockAnchor::face)
					).apply(instance, ::BlockAnchor)
				}

			val STREAM_CODEC: StreamCodec<ByteBuf, BlockAnchor> = StreamCodec.composite(
				BlockPos.STREAM_CODEC, BlockAnchor::blockPos,
				Direction.STREAM_CODEC, BlockAnchor::face,
				::BlockAnchor
			)
		}
	}

	data class LineAnchor(
		val lineUuid: UUID,
		val percentAlong: Double
	) : WebNode {
		companion object {
			const val TYPE = "line"
			const val TYPE_ID = 1

			val CODEC: MapCodec<LineAnchor> =
				RecordCodecBuilder.mapCodec { instance ->
					instance.group(
						UUIDUtil.CODEC
							.fieldOf("line_uuid")
							.forGetter(LineAnchor::lineUuid),
						Codec.doubleRange(0.0, 1.0)
							.fieldOf("percent_along")
							.forGetter(LineAnchor::percentAlong)
					).apply(instance, ::LineAnchor)
				}

			val STREAM_CODEC: StreamCodec<ByteBuf, LineAnchor> = StreamCodec.composite(
				UUIDUtil.STREAM_CODEC, LineAnchor::lineUuid,
				ByteBufCodecs.DOUBLE, LineAnchor::percentAlong,
				::LineAnchor
			)
		}
	}

	companion object {
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