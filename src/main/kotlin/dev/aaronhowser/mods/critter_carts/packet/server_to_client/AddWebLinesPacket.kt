package dev.aaronhowser.mods.critter_carts.packet.server_to_client

import dev.aaronhowser.mods.aaron.packet.AaronPacket
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.handler.web.ClientWebLines
import dev.aaronhowser.mods.critter_carts.handler.web.WebLine
import dev.aaronhowser.mods.critter_carts.handler.web.WebLineData
import dev.aaronhowser.mods.critter_carts.handler.web.node.WebNode
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.neoforged.neoforge.network.handling.IPayloadContext

class AddWebLinesPacket(
	val nodes: List<WebNode>,
	val lines: List<WebLineData>
) : AaronPacket() {

	override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
		return TYPE
	}

	override fun handleOnClient(context: IPayloadContext) {
		ClientWebLines.addLines(nodes, lines)
	}

	companion object {
		val TYPE: CustomPacketPayload.Type<AddWebLinesPacket> =
			makeType(CritterCarts.MOD_ID, "add_web_lines")

		val STREAM_CODEC: StreamCodec<ByteBuf, AddWebLinesPacket> = StreamCodec.composite(
			WebNode.STREAM_CODEC.apply(ByteBufCodecs.list()), AddWebLinesPacket::nodes,
			WebLineData.STREAM_CODEC.apply(ByteBufCodecs.list()), AddWebLinesPacket::lines,
			::AddWebLinesPacket
		)

		fun fromLines(lines: Collection<WebLine>): AddWebLinesPacket {
			val nodes = lines
				.flatMap { line -> listOf(line.firstNode, line.secondNode) }
				.distinctBy(WebNode::uuid)

			return AddWebLinesPacket(nodes, lines.map(WebLine::data))
		}
	}
}