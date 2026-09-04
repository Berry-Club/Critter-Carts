package dev.aaronhowser.mods.critterworks.entity.attachment.builtin

import dev.aaronhowser.mods.critterworks.entity.attachment.ScoochwormAttachment
import dev.aaronhowser.mods.critterworks.entity.attachment.data.NoAttachmentData
import dev.aaronhowser.mods.critterworks.entity.attachment.data.SyncedAttachmentData
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.ItemStack

class NoAttachment : ScoochwormAttachment(ItemStack.EMPTY) {

	override val syncedData: SyncedAttachmentData = NoAttachmentData
	override val equipSound: SoundEvent? = null
}