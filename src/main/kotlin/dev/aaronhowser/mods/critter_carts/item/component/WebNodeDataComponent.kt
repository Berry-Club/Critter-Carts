package dev.aaronhowser.mods.critter_carts.item.component

import com.mojang.serialization.Codec
import dev.aaronhowser.mods.critter_carts.handler.web.node.WebNode
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec

data class WebNodeDataComponent(
	val node: WebNode
) {
	companion object {
		val CODEC: Codec<WebNodeDataComponent> =
			WebNode.CODEC.xmap(
				::WebNodeDataComponent,
				WebNodeDataComponent::node
			)

		val STREAM_CODEC: StreamCodec<ByteBuf, WebNodeDataComponent> =
			WebNode.STREAM_CODEC.map(
				::WebNodeDataComponent,
				WebNodeDataComponent::node
			)
	}
}