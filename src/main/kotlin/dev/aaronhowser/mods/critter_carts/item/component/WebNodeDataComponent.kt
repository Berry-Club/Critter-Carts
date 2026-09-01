package dev.aaronhowser.mods.critter_carts.item.component

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.aaronhowser.mods.critter_carts.handler.web.WebNode
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec

data class WebNodeDataComponent(
	val node: WebNode
) {
	companion object {
		val CODEC: Codec<WebNodeDataComponent> = RecordCodecBuilder.create { instance ->
			instance.group(
				WebNode.CODEC
					.fieldOf("node")
					.forGetter(WebNodeDataComponent::node)
			).apply(instance, ::WebNodeDataComponent)
		}

		val STREAM_CODEC: StreamCodec<ByteBuf, WebNodeDataComponent> = StreamCodec.composite(
			WebNode.STREAM_CODEC,
			WebNodeDataComponent::node,
			::WebNodeDataComponent
		)
	}
}