package dev.aaronhowser.mods.critter_carts.item

import dev.aaronhowser.mods.critter_carts.datagen.language.ModMenuLang
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.tooltip.BundleTooltip
import net.minecraft.world.inventory.tooltip.TooltipComponent
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.BundleContents
import net.minecraft.world.item.component.ItemContainerContents
import java.util.*

class SaddlebagItem(
	properties: Properties
) : Item(properties) {

	override fun getTooltipImage(stack: ItemStack): Optional<TooltipComponent> {
		val containerContents = stack.getOrDefault(
			DataComponents.CONTAINER,
			ItemContainerContents.EMPTY
		)

		val storedItems = containerContents.nonEmptyItemsCopy().toList()
		val bundleContents = BundleContents(storedItems)

		return Optional.of(BundleTooltip(bundleContents))
	}

	override fun appendHoverText(
		stack: ItemStack,
		context: TooltipContext,
		tooltipComponents: MutableList<Component>,
		tooltipFlag: TooltipFlag
	) {
		tooltipComponents.add(Component.translatable(ModMenuLang.SADDLE_NOT_INCLUDED))
	}
}