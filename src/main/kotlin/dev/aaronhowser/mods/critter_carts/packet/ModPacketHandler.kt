package dev.aaronhowser.mods.critter_carts.packet

import dev.aaronhowser.mods.aaron.packet.AaronPacketRegistrar
import dev.aaronhowser.mods.critter_carts.packet.server_to_client.AddWebLinesPacket
import dev.aaronhowser.mods.critter_carts.packet.server_to_client.RemoveWebLinePacket
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent

object ModPacketHandler : AaronPacketRegistrar {

	fun registerPayloads(event: RegisterPayloadHandlersEvent) {
		val registrar = event.registrar("1")

		toClient(registrar, AddWebLinesPacket.TYPE, AddWebLinesPacket.STREAM_CODEC)
		toClient(registrar, RemoveWebLinePacket.TYPE, RemoveWebLinePacket.STREAM_CODEC)
	}
}