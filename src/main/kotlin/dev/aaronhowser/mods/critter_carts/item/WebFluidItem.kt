package dev.aaronhowser.mods.critter_carts.item

import dev.aaronhowser.mods.critter_carts.handler.web.WebLineInteractionHandler
import dev.aaronhowser.mods.critter_carts.handler.web.node.BlockAnchor
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.phys.Vec3

class WebFluidItem(properties: Properties) : Item(properties) {

	override fun useOn(context: UseOnContext): InteractionResult {
		val level = context.level
		if (level !is ServerLevel) return InteractionResult.SUCCESS

		val player = context.player ?: return InteractionResult.PASS
		val blockAnchor = createBlockAnchor(context)
		WebLineInteractionHandler.handleNodeSelection(level, player, context.itemInHand, blockAnchor)
		return InteractionResult.CONSUME
	}

	private fun createBlockAnchor(context: UseOnContext): BlockAnchor {
		val surfaceOffset = 0.001
		val faceNormal = Vec3.atLowerCornerOf(context.clickedFace.normal)
		val position = context.clickLocation.add(faceNormal.scale(surfaceOffset))

		return BlockAnchor(context.clickedPos, context.clickedFace, position)
	}
}