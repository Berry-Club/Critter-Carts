package dev.aaronhowser.mods.critter_carts.item

import dev.aaronhowser.mods.critter_carts.handler.web.WebLineInteractionHandler
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext

class WebFluidItem(properties: Properties) : Item(properties) {

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
}