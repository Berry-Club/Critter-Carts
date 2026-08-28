package dev.aaronhowser.mods.critter_carts.entity.attachment.builtin

import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import dev.aaronhowser.mods.critter_carts.entity.attachment.AttachmentInteractionResult
import dev.aaronhowser.mods.critter_carts.entity.attachment.ScoochwormAttachment
import dev.aaronhowser.mods.critter_carts.entity.attachment.data.SaddleAttachmentData
import dev.aaronhowser.mods.critter_carts.entity.attachment.data.SyncedAttachmentData
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

class SaddleAttachment(
	saddle: ItemStack
) : ScoochwormAttachment(saddle) {

	override val syncedData: SyncedAttachmentData = SaddleAttachmentData
	override val equipSound: SoundEvent = SoundEvents.HORSE_SADDLE

	override fun interact(
		player: Player,
		heldStack: ItemStack,
		bodyPart: ScoochwormPartEntity
	): AttachmentInteractionResult {
		if (player.isShiftKeyDown) return AttachmentInteractionResult.Pass
		if (!player.startRiding(bodyPart)) return AttachmentInteractionResult.Pass
		return AttachmentInteractionResult.Consume
	}

	override fun predictInteraction(
		player: Player,
		heldStack: ItemStack,
		bodyPart: ScoochwormPartEntity
	): InteractionResult {
		return if (player.isShiftKeyDown) InteractionResult.PASS else InteractionResult.SUCCESS
	}
}