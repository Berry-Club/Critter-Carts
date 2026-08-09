package dev.aaronhowser.mods.critter_carts.entity.data.attachment

import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

class NoAttachment : ScoochwormAttachment(ItemStack.EMPTY) {

	override val type = ScoochwormAttachmentType.NONE
	override val equipSound: SoundEvent? = null

	override fun interact(
		player: Player,
		heldStack: ItemStack,
		bodyPart: ScoochwormPartEntity
	): AttachmentInteractionResult {
		if (!canAttach(heldStack)) {
			return AttachmentInteractionResult.Pass
		}

		val attachmentItem = heldStack.copy()
		attachmentItem.count = 1
		return AttachmentInteractionResult.Install(attachmentItem)
	}
}