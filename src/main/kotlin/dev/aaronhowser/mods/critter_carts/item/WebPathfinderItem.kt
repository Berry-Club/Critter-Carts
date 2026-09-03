package dev.aaronhowser.mods.critter_carts.item

import dev.aaronhowser.mods.critter_carts.registry.ModDataComponents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class WebPathfinderItem(properties: Properties) : Item(properties) {

	override fun use(
		level: Level,
		player: Player,
		usedHand: InteractionHand
	): InteractionResultHolder<ItemStack> {
		val usedStack = player.getItemInHand(usedHand)
		if (player.isSecondaryUseActive && usedStack.has(ModDataComponents.WEB_NODE)) {
			usedStack.remove(ModDataComponents.WEB_NODE)
			return InteractionResultHolder.sidedSuccess(usedStack, level.isClientSide)
		}

		return super.use(level, player, usedHand)
	}

	override fun isFoil(stack: ItemStack): Boolean {
		return stack.has(ModDataComponents.WEB_NODE)
	}

}