package dev.aaronhowser.mods.critter_carts.entity.attachment

import dev.aaronhowser.mods.critter_carts.entity.attachment.data.SyncedAttachmentData

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

class ScoochwormAttachmentType<T : SyncedAttachmentData>(
	private val streamCodec: StreamCodec<ByteBuf, T>,
	private val itemPredicate: (ItemStack) -> Boolean,
	private val attachmentFactory: (ItemStack) -> ScoochwormAttachment,
	private val clientAttachmentFactory: () -> ScoochwormAttachment,
	private val interactionPredictor: (T, Player, ItemStack) -> InteractionResult
) {
	fun createAttachment(itemStack: ItemStack): ScoochwormAttachment? {
		if (!itemPredicate(itemStack)) return null
		return attachmentFactory(itemStack)
	}

	fun accepts(itemStack: ItemStack): Boolean {
		return itemPredicate(itemStack)
	}

	fun createClientAttachment(): ScoochwormAttachment {
		return clientAttachmentFactory()
	}

	@Suppress("UNCHECKED_CAST")
	fun predictInteraction(
		data: SyncedAttachmentData,
		player: Player,
		heldStack: ItemStack
	): InteractionResult {
		return interactionPredictor(data as T, player, heldStack)
	}

	internal fun decode(buffer: ByteBuf): T {
		return streamCodec.decode(buffer)
	}

	@Suppress("UNCHECKED_CAST")
	internal fun encode(buffer: ByteBuf, data: SyncedAttachmentData) {
		streamCodec.encode(buffer, data as T)
	}
}