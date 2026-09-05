package dev.aaronhowser.mods.critterworks.item

import dev.aaronhowser.mods.critterworks.handler.web.WebLineInteractionHandler
import dev.aaronhowser.mods.critterworks.registry.ModDataComponents
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level

class ArtificialSpinneretsItem(properties: Properties) : Item(properties) {

	override fun useOn(context: UseOnContext): InteractionResult {
		val level = context.level
		if (level !is ServerLevel) return InteractionResult.SUCCESS

		val player = context.player ?: return InteractionResult.PASS
		val blockAnchor = WebLineInteractionHandler.createBlockAnchor(
			context.clickedPos,
			context.clickedFace,
			context.clickLocation
		)

		WebLineInteractionHandler.handleNodeSelection(level, player, context.itemInHand, blockAnchor)
		return InteractionResult.CONSUME
	}

	override fun use(
		level: Level,
		player: Player,
		usedHand: InteractionHand
	): InteractionResultHolder<ItemStack> {
		if (player.isSecondaryUseActive) {
			val usedStack = player.getItemInHand(usedHand)
			if (usedStack.has(ModDataComponents.WEB_NODE)) {
				usedStack.remove(ModDataComponents.WEB_NODE)
				return InteractionResultHolder.sidedSuccess(usedStack, level.isClientSide)
			}
		}

		return super.use(level, player, usedHand)
	}

	override fun isFoil(stack: ItemStack): Boolean {
		return stack.has(ModDataComponents.WEB_NODE)
	}

}