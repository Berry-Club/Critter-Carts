package dev.aaronhowser.mods.critter_carts.item

import net.minecraft.core.component.DataComponents
import net.minecraft.world.inventory.tooltip.BundleTooltip
import net.minecraft.world.inventory.tooltip.TooltipComponent
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.BundleContents
import net.minecraft.world.item.component.ItemContainerContents
import java.util.*

class WickerBasketItem(
	properties: Properties
) : Item(properties) {

	override fun getTooltipImage(stack: ItemStack): Optional<TooltipComponent> {
		if (stack.has(DataComponents.HIDE_TOOLTIP)) return Optional.empty()
		if (stack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)) return Optional.empty()

		val containerContents = stack.getOrDefault(
			DataComponents.CONTAINER,
			ItemContainerContents.EMPTY
		)
		val storedItems = containerContents.nonEmptyItemsCopy().toList()
		val bundleContents = BundleContents(storedItems)
		return Optional.of(BundleTooltip(bundleContents))
	}

	companion object {
		val DEFAULT_PROPERTIES: () -> Properties = {
			Properties()
				.stacksTo(1)
				.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
		}
	}

}