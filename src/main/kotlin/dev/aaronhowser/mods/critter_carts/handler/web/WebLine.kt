package dev.aaronhowser.mods.critter_carts.handler.web

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.core.UUIDUtil
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.level.ChunkPos
import java.util.UUID

data class WebLine(
	val uuid: UUID,
	val firstNode: WebNode,
	val secondNode: WebNode
) {
	fun getChunkPositions(): Set<ChunkPos> {
		return listOf(firstNode, secondNode)
			.filterIsInstance<WebNode.BlockAnchor>()
			.mapTo(mutableSetOf()) { node -> ChunkPos(node.blockPos) }
	}

	companion object {
		val CODEC: Codec<WebLine> = RecordCodecBuilder.create { instance ->
			instance.group(
				UUIDUtil.CODEC
					.fieldOf("uuid")
					.forGetter(WebLine::uuid),
				WebNode.CODEC
					.fieldOf("first_node")
					.forGetter(WebLine::firstNode),
				WebNode.CODEC
					.fieldOf("second_node")
					.forGetter(WebLine::secondNode)
			).apply(instance, ::WebLine)
		}

		val STREAM_CODEC: StreamCodec<ByteBuf, WebLine> = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, WebLine::uuid,
			WebNode.STREAM_CODEC, WebLine::firstNode,
			WebNode.STREAM_CODEC, WebLine::secondNode,
			::WebLine
		)
	}
}