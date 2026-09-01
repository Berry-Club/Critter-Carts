package dev.aaronhowser.mods.critter_carts.item

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.status
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.toComponent
import dev.aaronhowser.mods.critter_carts.datagen.language.ModMessageLang
import dev.aaronhowser.mods.critter_carts.handler.web.WebLine
import dev.aaronhowser.mods.critter_carts.handler.web.WebNode
import dev.aaronhowser.mods.critter_carts.handler.web.WebSavedData
import dev.aaronhowser.mods.critter_carts.item.component.WebNodeDataComponent
import dev.aaronhowser.mods.critter_carts.registry.ModDataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import java.util.UUID

class WebFluidItem(properties: Properties) : Item(properties) {

	override fun useOn(context: UseOnContext): InteractionResult {
		val level = context.level
		if (level !is ServerLevel) return InteractionResult.SUCCESS

		val player = context.player ?: return InteractionResult.PASS
		val itemStack = context.itemInHand
		val selectedNode = WebNode.BlockAnchor(context.clickedPos, context.clickedFace)
		val storedNode = itemStack.get(ModDataComponents.WEB_NODE)?.node

		if (storedNode == null) {
			itemStack.set(ModDataComponents.WEB_NODE, WebNodeDataComponent(selectedNode))
			player.status(ModMessageLang.FIRST_NODE_MESSAGE.toComponent())
			return InteractionResult.CONSUME
		}

		if (storedNode !is WebNode.BlockAnchor) {
			itemStack.remove(ModDataComponents.WEB_NODE)
			return InteractionResult.CONSUME
		}

		if (storedNode.face == selectedNode.face) {
			player.status(ModMessageLang.SAME_DIRECTION_MESSAGE.toComponent())
			return InteractionResult.CONSUME
		}

		val firstCenter = getFaceCenter(storedNode)
		val secondCenter = getFaceCenter(selectedNode)
		if (firstCenter.distanceToSqr(secondCenter) >= MAX_LENGTH_SQUARED) {
			player.status(ModMessageLang.TOO_LONG_MESSAGE.toComponent())
			return InteractionResult.CONSUME
		}

		if (!hasLineOfSight(level, player, storedNode, selectedNode)) {
			player.status(ModMessageLang.OBSTRUCTED_MESSAGE.toComponent())
			return InteractionResult.CONSUME
		}

		val line = WebLine(UUID.randomUUID(), storedNode, selectedNode)
		WebSavedData.get(level).addLine(level, line)
		itemStack.remove(ModDataComponents.WEB_NODE)
		player.status(ModMessageLang.LINE_CREATED_MESSAGE.toComponent())

		return InteractionResult.CONSUME
	}

	private fun hasLineOfSight(
		level: ServerLevel,
		player: Player,
		firstNode: WebNode.BlockAnchor,
		secondNode: WebNode.BlockAnchor
	): Boolean {
		val firstCenter = offsetFromFace(getFaceCenter(firstNode), firstNode)
		val secondCenter = offsetFromFace(getFaceCenter(secondNode), secondNode)
		val clipContext = ClipContext(
			firstCenter,
			secondCenter,
			ClipContext.Block.COLLIDER,
			ClipContext.Fluid.NONE,
			player
		)
		val result = level.clip(clipContext)
		return result.type == HitResult.Type.MISS
	}

	private fun getFaceCenter(node: WebNode.BlockAnchor): Vec3 {
		val blockCenter = Vec3.atCenterOf(node.blockPos)
		val faceOffset = Vec3.atLowerCornerOf(node.face.normal).scale(0.5)
		return blockCenter.add(faceOffset)
	}

	private fun offsetFromFace(center: Vec3, node: WebNode.BlockAnchor): Vec3 {
		val faceOffset = Vec3.atLowerCornerOf(node.face.normal).scale(SURFACE_OFFSET)
		return center.add(faceOffset)
	}

	companion object {
		private const val MAX_LENGTH = 10.0
		private const val MAX_LENGTH_SQUARED = MAX_LENGTH * MAX_LENGTH
		private const val SURFACE_OFFSET = 0.001
	}
}