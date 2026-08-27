package dev.aaronhowser.mods.critter_carts.entity.attachment.builtin

import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import dev.aaronhowser.mods.critter_carts.entity.attachment.AttachmentInteractionResult
import dev.aaronhowser.mods.critter_carts.entity.attachment.ScoochwormAttachment
import dev.aaronhowser.mods.critter_carts.entity.attachment.data.NoAttachmentData
import dev.aaronhowser.mods.critter_carts.entity.attachment.data.SynchedAttachmentData
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

class NoAttachment : ScoochwormAttachment(ItemStack.EMPTY) {

	override val synchedData: SynchedAttachmentData = NoAttachmentData
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