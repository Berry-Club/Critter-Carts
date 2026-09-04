package dev.aaronhowser.mods.critterworks.packet.client_to_server

import dev.aaronhowser.mods.aaron.packet.AaronPacket
import dev.aaronhowser.mods.critterworks.Critterworks
import dev.aaronhowser.mods.critterworks.menu.nest_interface.NestInterfaceMenu
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.network.handling.IPayloadContext

class SetNestInterfacePriorityPacket(
	val priority: Int
) : AaronPacket() {

	override fun handleOnServer(context: IPayloadContext) {
		val player = context.player() as? ServerPlayer ?: return
		val menu = player.containerMenu as? NestInterfaceMenu ?: return
		menu.setPriority(priority)
	}

	override fun type(): CustomPacketPayload.Type<SetNestInterfacePriorityPacket> = TYPE

	companion object {
		val TYPE: CustomPacketPayload.Type<SetNestInterfacePriorityPacket> =
			makeType(Critterworks.MOD_ID, "set_nest_interface_priority")

		val STREAM_CODEC: StreamCodec<ByteBuf, SetNestInterfacePriorityPacket> =
			ByteBufCodecs.VAR_INT.map(::SetNestInterfacePriorityPacket, SetNestInterfacePriorityPacket::priority)
	}
}