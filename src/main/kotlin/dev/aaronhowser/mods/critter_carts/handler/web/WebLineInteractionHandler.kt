package dev.aaronhowser.mods.critter_carts.handler.web

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.getEquipmentSlot
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isItem
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.status
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.toComponent
import dev.aaronhowser.mods.critter_carts.datagen.language.ModMessageLang
import dev.aaronhowser.mods.critter_carts.datagen.tag.ModItemTagsProvider
import dev.aaronhowser.mods.critter_carts.handler.web.line.WebLine
import dev.aaronhowser.mods.critter_carts.handler.web.node.BlockAnchor
import dev.aaronhowser.mods.critter_carts.handler.web.node.LineAnchor
import dev.aaronhowser.mods.critter_carts.handler.web.node.WebNode
import dev.aaronhowser.mods.critter_carts.item.component.WebNodeDataComponent
import dev.aaronhowser.mods.critter_carts.registry.ModDataComponents
import dev.aaronhowser.mods.critter_carts.registry.ModItems
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.common.Tags
import org.joml.Intersectiond
import org.joml.Vector3d
import java.util.*

object WebLineInteractionHandler {

	private const val LINE_SELECTION_RADIUS = 0.3
	private const val REQUESTED_POSITION_TOLERANCE = 0.1

	fun interact(
		player: ServerPlayer,
		targetUuid: UUID,
		targetsNode: Boolean,
		requestedPosition: Vec3,
		hand: InteractionHand
	) {
		val itemStack = player.getItemInHand(hand)
		if (!itemStack.isItem(ModItemTagsProvider.WEB_LINE_INTERACTABLE)) return

		val level = player.serverLevel()
		val savedData = WebSavedData.get(level)
		if (targetsNode) {
			if (!itemStack.isItem(ModItems.WEB_FLUID)) return

			val selectedNode = savedData.getNode(targetUuid) ?: return
			if (!isTargetingNode(player, selectedNode)) return
			val positionToleranceSquared =
				REQUESTED_POSITION_TOLERANCE * REQUESTED_POSITION_TOLERANCE
			if (selectedNode.position.distanceToSqr(requestedPosition) > positionToleranceSquared) return

			handleNodeSelection(level, player, itemStack, selectedNode)
			return
		}

		val line = savedData.getLine(targetUuid) ?: return
		val eyePosition = player.eyePosition
		val interactionRange = player.blockInteractionRange()
		val lookOffset = player.lookAngle.scale(interactionRange)
		val lookEnd = eyePosition.add(lookOffset)
		val snapToExistingNode = itemStack.isItem(ModItems.WEB_FLUID)
		val targetedNode = getTargetedNode(
			listOf(line),
			eyePosition,
			lookEnd,
			snapToExistingNode
		) ?: return
		val selectedNode = targetedNode.node
		val positionToleranceSquared =
			REQUESTED_POSITION_TOLERANCE * REQUESTED_POSITION_TOLERANCE

		if (selectedNode.position.distanceToSqr(requestedPosition) > positionToleranceSquared) return

		when {
			itemStack.isItem(Tags.Items.TOOLS_SHEAR) && selectedNode is LineAnchor ->
				shearLine(level, player, itemStack, targetUuid, selectedNode, hand)

			itemStack.isItem(ModItems.WEB_FLUID) ->
				handleNodeSelection(level, player, itemStack, selectedNode)
		}
	}

	fun handleNodeSelection(
		level: ServerLevel,
		player: Player,
		itemStack: ItemStack,
		selectedNode: WebNode
	) {
		val firstNode = itemStack.get(ModDataComponents.WEB_NODE)?.node

		if (firstNode == null) {
			storeFirstNode(player, itemStack, selectedNode)
		} else {
			createLine(level, player, itemStack, firstNode, selectedNode)
		}
	}

	private fun createLine(
		level: ServerLevel,
		player: Player,
		itemStack: ItemStack,
		firstNode: WebNode,
		selectedNode: WebNode
	) {
		val savedData = WebSavedData.get(level)
		val canonicalFirstNode = savedData.getCanonicalNode(firstNode)
		val canonicalSelectedNode = savedData.getCanonicalNode(selectedNode)
		val invalidMessage = getInvalidMessage(
			level,
			player,
			canonicalFirstNode,
			canonicalSelectedNode
		)
		if (invalidMessage != null) {
			player.status(invalidMessage.toComponent())
			return
		}

		val webLine = WebLine(UUID.randomUUID(), canonicalFirstNode, canonicalSelectedNode)
		savedData.addLine(level, webLine)
		itemStack.remove(ModDataComponents.WEB_NODE)
		player.status(ModMessageLang.LINE_CREATED_MESSAGE.toComponent())
	}

	private fun shearLine(
		level: ServerLevel,
		player: ServerPlayer,
		itemStack: ItemStack,
		lineUuid: UUID,
		selectedNode: LineAnchor,
		hand: InteractionHand
	) {
		WebSavedData.get(level).removeLine(level, lineUuid)
		itemStack.hurtAndBreak(1, player, hand.getEquipmentSlot())

		player.status(ModMessageLang.LINE_REMOVED_MESSAGE.toComponent())

		level.gameEvent(player, GameEvent.SHEAR, selectedNode.position)
	}

	private fun storeFirstNode(player: Player, itemStack: ItemStack, selectedNode: WebNode) {
		itemStack.set(ModDataComponents.WEB_NODE, WebNodeDataComponent(selectedNode))
		player.status(ModMessageLang.FIRST_NODE_MESSAGE.toComponent())
	}

	fun canCreateLine(
		level: Level,
		player: Player,
		firstNode: WebNode,
		secondNode: WebNode
	): Boolean {
		return getInvalidMessage(level, player, firstNode, secondNode) == null
	}

	fun createBlockAnchor(blockPos: BlockPos, face: Direction, position: Vec3): BlockAnchor {
		val surfaceOffset = 0.001
		val faceNormal = Vec3.atLowerCornerOf(face.normal)

		return BlockAnchor(
			UUID.randomUUID(),
			blockPos,
			face,
			position.add(faceNormal.scale(surfaceOffset))
		)
	}

	private fun getInvalidMessage(
		level: Level,
		player: Player,
		firstNode: WebNode,
		secondNode: WebNode
	): String? {
		if (firstNode.uuid == secondNode.uuid) return ModMessageLang.SAME_LINE_MESSAGE

		if (firstNode is LineAnchor
			&& secondNode is LineAnchor
			&& firstNode.lineUuid == secondNode.lineUuid
		) return ModMessageLang.SAME_LINE_MESSAGE

		if (firstNode is BlockAnchor
			&& secondNode is BlockAnchor
			&& firstNode.face == secondNode.face
		) return ModMessageLang.SAME_DIRECTION_MESSAGE

		val maxLength = 10.0
		if (firstNode.position.distanceToSqr(secondNode.position) >= maxLength * maxLength) {
			return ModMessageLang.TOO_LONG_MESSAGE
		}

		if (!hasLineOfSight(level, player, firstNode, secondNode)) {
			return ModMessageLang.OBSTRUCTED_MESSAGE
		}

		return null
	}

	fun getTargetedNode(
		lines: List<WebLine>,
		lookStart: Vec3,
		lookEnd: Vec3,
		snapToExistingNode: Boolean
	): TargetedWebNode? {
		val selectionRadiusSquared = LINE_SELECTION_RADIUS * LINE_SELECTION_RADIUS
		var targetedNode: TargetedWebNode? = null
		var targetedDistanceSquared = selectionRadiusSquared

		for (line in lines) {
			val lineStart = line.firstNode.position
			val lineEnd = line.secondNode.position
			val anchorPosition = Vector3d()
			val lookPosition = Vector3d()

			val distanceFromLookSquared = Intersectiond.findClosestPointsLineSegments(
				lineStart.x, lineStart.y, lineStart.z,
				lineEnd.x, lineEnd.y, lineEnd.z,
				lookStart.x, lookStart.y, lookStart.z,
				lookEnd.x, lookEnd.y, lookEnd.z,
				anchorPosition,
				lookPosition
			)

			if (distanceFromLookSquared > targetedDistanceSquared) continue

			targetedDistanceSquared = distanceFromLookSquared
			val position = Vec3(anchorPosition.x, anchorPosition.y, anchorPosition.z)
			val node = if (snapToExistingNode) {
				getSnappedNode(line, position)
			} else {
				LineAnchor(UUID.randomUUID(), line.uuid, position)
			}

			val targetedLineUuid = if (node === line.firstNode || node === line.secondNode) {
				null
			} else {
				line.uuid
			}

			targetedNode = TargetedWebNode(targetedLineUuid, node)
		}

		return targetedNode
	}

	private fun getSnappedNode(line: WebLine, position: Vec3): WebNode {
		val snapRadiusSquared = NODE_SNAP_RADIUS * NODE_SNAP_RADIUS
		val firstDistanceSquared = line.firstNode.position.distanceToSqr(position)
		val secondDistanceSquared = line.secondNode.position.distanceToSqr(position)

		if (firstDistanceSquared <= snapRadiusSquared
			&& firstDistanceSquared <= secondDistanceSquared
		) return line.firstNode

		if (secondDistanceSquared <= snapRadiusSquared) return line.secondNode

		return LineAnchor(UUID.randomUUID(), line.uuid, position)
	}

	private fun hasLineOfSight(
		level: Level,
		player: Player,
		firstNode: WebNode,
		secondNode: WebNode
	): Boolean {
		val clipContext = ClipContext(
			firstNode.position,
			secondNode.position,
			ClipContext.Block.COLLIDER,
			ClipContext.Fluid.NONE,
			player
		)
		val result = level.clip(clipContext)
		return result.type == HitResult.Type.MISS
	}

	private fun isTargetingNode(player: Player, node: WebNode): Boolean {
		val lookStart = player.eyePosition
		val lookOffset = player.lookAngle.scale(player.blockInteractionRange())
		val lookLengthSquared = lookOffset.lengthSqr()
		if (lookLengthSquared == 0.0) return false

		val nodeOffset = node.position.subtract(lookStart)
		val progress = nodeOffset.dot(lookOffset)
			.div(lookLengthSquared)
			.coerceIn(0.0, 1.0)
		val closestPosition = lookStart.add(lookOffset.scale(progress))
		val selectionRadiusSquared = LINE_SELECTION_RADIUS * LINE_SELECTION_RADIUS

		return closestPosition.distanceToSqr(node.position) <= selectionRadiusSquared
	}

	private const val NODE_SNAP_RADIUS = 0.3

}