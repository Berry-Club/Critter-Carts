package dev.aaronhowser.mods.critterworks.item

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isServerSide
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.toComponent
import dev.aaronhowser.mods.critterworks.datagen.language.ModMenuLang
import dev.aaronhowser.mods.critterworks.item.component.WebPortComponent
import dev.aaronhowser.mods.critterworks.menu.web_port.WebPortMenu
import dev.aaronhowser.mods.critterworks.registry.ModDataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.MenuConstructor
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

class WebPortItem(properties: Properties) : Item(properties) {

	override fun use(
		level: Level,
		player: Player,
		usedHand: InteractionHand
	): InteractionResultHolder<ItemStack?> {
		val usedStack = player.getItemInHand(usedHand)

		if (level.isServerSide) {
			val constructor = MenuConstructor { containerId, inventory, _ ->
				WebPortMenu(containerId, inventory, usedHand)
			}

			player.openMenu(SimpleMenuProvider(constructor, usedStack.hoverName)) {
				it.writeBoolean(false)
				it.writeEnum(usedHand)
			}
		}

		return InteractionResultHolder.sidedSuccess(usedStack, level.isClientSide)
	}

	override fun appendHoverText(
		stack: ItemStack,
		context: TooltipContext,
		tooltipComponents: MutableList<Component>,
		tooltipFlag: TooltipFlag
	) {
		val component = getComponent(stack)
		val color = ModMenuLang.WEB_PORT_COLOR_TOOLTIP.toComponent(component.color.name)
		val directionKey = when (component.transferDirection) {
			WebPortComponent.TransferDirection.INPUT -> ModMenuLang.WEB_PORT_INPUT_TOOLTIP
			WebPortComponent.TransferDirection.OUTPUT -> ModMenuLang.WEB_PORT_OUTPUT_TOOLTIP
		}
		val direction = directionKey.toComponent()
		val priority = ModMenuLang.WEB_PORT_PRIORITY_TOOLTIP.toComponent(component.priority)

		tooltipComponents.add(color)
		tooltipComponents.add(direction)
		tooltipComponents.add(priority)
	}

	companion object {
		fun getComponent(stack: ItemStack): WebPortComponent {
			return stack.get(ModDataComponents.WEB_PORT) ?: WebPortComponent()
		}

		fun setFilter(stack: ItemStack, filter: ItemStack) {
			stack.set(ModDataComponents.WEB_PORT, getComponent(stack).withFilter(filter))
		}

		fun setColor(stack: ItemStack, color: DyeColor) {
			stack.set(ModDataComponents.WEB_PORT, getComponent(stack).withColor(color))
		}

		fun setTransferDirection(stack: ItemStack, direction: WebPortComponent.TransferDirection) {
			stack.set(ModDataComponents.WEB_PORT, getComponent(stack).withTransferDirection(direction))
		}

		fun setPriority(stack: ItemStack, priority: Int) {
			stack.set(ModDataComponents.WEB_PORT, getComponent(stack).withPriority(priority))
		}
	}
}