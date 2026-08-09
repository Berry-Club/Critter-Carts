package dev.aaronhowser.mods.critter_carts.item

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.toGrayComponent
import dev.aaronhowser.mods.critter_carts.datagen.language.ModMenuLang
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.ItemContainerContents

class StorageAttachmentItem(
	properties: Properties,
	private val additionalTooltip: Component?
) : Item(properties) {

	override fun appendHoverText(
		stack: ItemStack,
		context: TooltipContext,
		tooltipComponents: MutableList<Component>,
		tooltipFlag: TooltipFlag
	) {
		val containerContents = stack.getOrDefault(
			DataComponents.CONTAINER,
			ItemContainerContents.EMPTY
		)

		val contentsText = when (
			val stackCount = containerContents.nonEmptyItemsCopy().count()
		) {
			0 -> ModMenuLang.CONTAINER_EMPTY.toGrayComponent()
			1 -> ModMenuLang.CONTAINER_STACK.toGrayComponent(stackCount)
			else -> ModMenuLang.CONTAINER_STACKS.toGrayComponent(stackCount)
		}

		tooltipComponents += contentsText.withStyle(ChatFormatting.GRAY)

		if (additionalTooltip != null) {
			tooltipComponents += additionalTooltip
		}
	}

	companion object {
		val DEFAULT_PROPERTIES: () -> Properties = {
			Properties()
				.stacksTo(1)
				.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
		}

		fun saddleBag(properties: Properties): StorageAttachmentItem {
			return StorageAttachmentItem(properties, ModMenuLang.SADDLE_NOT_INCLUDED.toGrayComponent())
		}

		fun wickerBasket(properties: Properties): StorageAttachmentItem {
			return StorageAttachmentItem(properties, null)
		}

	}
}