package dev.aaronhowser.mods.critterworks.item

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isServerSide
import dev.aaronhowser.mods.critterworks.item.component.NestInterfaceComponent
import dev.aaronhowser.mods.critterworks.menu.nest_interface.NestInterfaceMenu
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

class SpiderNestInterfaceItem(properties: Properties) : Item(properties) {

	override fun use(
		level: Level,
		player: Player,
		usedHand: InteractionHand
	): InteractionResultHolder<ItemStack?> {
		val usedStack = player.getItemInHand(usedHand)

		if (level.isServerSide) {
			val constructor = MenuConstructor { containerId, inventory, _ ->
				NestInterfaceMenu(containerId, inventory, usedHand)
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
		val color = Component.translatable(
			"tooltip.critterworks.interface.color",
			component.color.name
		)
		val direction = Component.translatable(
			"tooltip.critterworks.interface.${component.transferDirection.serializedName}"
		)
		val priority = Component.translatable(
			"tooltip.critterworks.interface.priority",
			component.priority
		)

		tooltipComponents.add(color)
		tooltipComponents.add(direction)
		tooltipComponents.add(priority)
	}

	companion object {
		fun getComponent(stack: ItemStack): NestInterfaceComponent {
			return stack.get(ModDataComponents.NEST_INTERFACE) ?: NestInterfaceComponent()
		}

		fun setFilter(stack: ItemStack, filter: ItemStack) {
			stack.set(ModDataComponents.NEST_INTERFACE, getComponent(stack).withFilter(filter))
		}

		fun setColor(stack: ItemStack, color: DyeColor) {
			stack.set(ModDataComponents.NEST_INTERFACE, getComponent(stack).withColor(color))
		}

		fun setTransferDirection(stack: ItemStack, direction: NestInterfaceComponent.TransferDirection) {
			stack.set(ModDataComponents.NEST_INTERFACE, getComponent(stack).withTransferDirection(direction))
		}

		fun setPriority(stack: ItemStack, priority: Int) {
			stack.set(ModDataComponents.NEST_INTERFACE, getComponent(stack).withPriority(priority))
		}
	}
}