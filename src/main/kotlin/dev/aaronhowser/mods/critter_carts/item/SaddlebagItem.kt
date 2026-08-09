package dev.aaronhowser.mods.critter_carts.item

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.toComponent
import dev.aaronhowser.mods.critter_carts.datagen.language.ModMenuLang
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

class SaddlebagItem(
	properties: Properties
) : Item(properties) {

	override fun appendHoverText(
		stack: ItemStack,
		context: TooltipContext,
		tooltipComponents: MutableList<Component>,
		tooltipFlag: TooltipFlag
	) {
		tooltipComponents += ModMenuLang.SADDLE_NOT_INCLUDED.toComponent()
	}
}