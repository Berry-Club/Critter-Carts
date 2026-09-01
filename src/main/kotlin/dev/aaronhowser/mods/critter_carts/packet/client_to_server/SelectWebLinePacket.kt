package dev.aaronhowser.mods.critter_carts.packet.client_to_server

import dev.aaronhowser.mods.aaron.packet.AaronPacket
import dev.aaronhowser.mods.aaron.serialization.AaronExtraStreamCodecs
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.item.WebFluidItem
import io.netty.buffer.ByteBuf
import net.minecraft.core.UUIDUtil
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.network.handling.IPayloadContext
import java.util.UUID

class SelectWebLinePacket(
	val lineUuid: UUID,
	val position: Vec3,
	val hand: InteractionHand
) : AaronPacket() {

	override fun handleOnServer(context: IPayloadContext) {
		val player = context.player() as? ServerPlayer ?: return
		WebFluidItem.selectLine(player, lineUuid, position, hand)
	}

	override fun type(): CustomPacketPayload.Type<SelectWebLinePacket> = TYPE

	companion object {
		val TYPE: CustomPacketPayload.Type<SelectWebLinePacket> =
			makeType(CritterCarts.MOD_ID, "select_web_line")

		val STREAM_CODEC: StreamCodec<ByteBuf, SelectWebLinePacket> = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, SelectWebLinePacket::lineUuid,
			AaronExtraStreamCodecs.VEC3, SelectWebLinePacket::position,
			ByteBufCodecs.BOOL.map(
				{ isMainHand -> if (isMainHand) InteractionHand.MAIN_HAND else InteractionHand.OFF_HAND },
				{ hand -> hand == InteractionHand.MAIN_HAND }
			), SelectWebLinePacket::hand,
			::SelectWebLinePacket
		)
	}
}