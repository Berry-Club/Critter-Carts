package dev.aaronhowser.mods.critter_carts.entity.attachment

import net.minecraft.world.item.ItemStack

sealed interface AttachmentInteractionResult {

	data object Pass : AttachmentInteractionResult
	data object Consume : AttachmentInteractionResult

	data class Install(
		val itemStack: ItemStack
	) : AttachmentInteractionResult
}