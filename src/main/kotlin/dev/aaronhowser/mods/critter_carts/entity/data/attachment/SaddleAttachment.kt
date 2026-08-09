package dev.aaronhowser.mods.critter_carts.entity.data.attachment

import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

class SaddleAttachment(
	saddle: ItemStack
) : ScoochwormAttachment(saddle) {

	override val type = ScoochwormAttachmentType.SADDLE
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
}