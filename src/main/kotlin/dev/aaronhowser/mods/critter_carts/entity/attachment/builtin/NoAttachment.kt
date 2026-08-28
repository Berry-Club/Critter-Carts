package dev.aaronhowser.mods.critter_carts.entity.attachment.builtin

import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import dev.aaronhowser.mods.critter_carts.entity.attachment.ScoochwormAttachment
import dev.aaronhowser.mods.critter_carts.entity.attachment.data.NoAttachmentData
import dev.aaronhowser.mods.critter_carts.entity.attachment.data.SyncedAttachmentData
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

class NoAttachment : ScoochwormAttachment(ItemStack.EMPTY) {

	override val syncedData: SyncedAttachmentData = NoAttachmentData
	override val equipSound: SoundEvent? = null

	override fun predictInteraction(
		player: Player,
		heldStack: ItemStack,
		bodyPart: ScoochwormPartEntity
	): InteractionResult {
		return if (canAttach(heldStack)) InteractionResult.SUCCESS else InteractionResult.PASS
	}
}