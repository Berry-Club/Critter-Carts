package dev.aaronhowser.mods.critter_carts.handler.web

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.getEquipmentSlot
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isItem
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.status
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.toComponent
import dev.aaronhowser.mods.critter_carts.datagen.language.ModMessageLang
import dev.aaronhowser.mods.critter_carts.datagen.tag.ModItemTagsProvider
import dev.aaronhowser.mods.critter_carts.handler.web.node.BlockAnchor
import dev.aaronhowser.mods.critter_carts.handler.web.node.LineAnchor
import dev.aaronhowser.mods.critter_carts.handler.web.node.WebNode
import dev.aaronhowser.mods.critter_carts.item.component.WebNodeDataComponent
import dev.aaronhowser.mods.critter_carts.registry.ModDataComponents
import dev.aaronhowser.mods.critter_carts.registry.ModItems
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ClipContext
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
		lineUuid: UUID,
		requestedPosition: Vec3,
		hand: InteractionHand
	) {
		val itemStack = player.getItemInHand(hand)
		if (!itemStack.isItem(ModItemTagsProvider.WEB_LINE_INTERACTABLE)) return

		val level = player.serverLevel()
		val line = WebSavedData.get(level).getLine(lineUuid) ?: return
		val eyePosition = player.eyePosition
		val interactionRange = player.blockInteractionRange()
		val lookOffset = player.lookAngle.scale(interactionRange)
		val lookEnd = eyePosition.add(lookOffset)
		val selectedNode = getTargetedLineAnchor(
			listOf(line),
			eyePosition,
			lookEnd
		) ?: return
		val positionToleranceSquared =
			REQUESTED_POSITION_TOLERANCE * REQUESTED_POSITION_TOLERANCE

		if (selectedNode.position.distanceToSqr(requestedPosition) > positionToleranceSquared) return

		when {
			itemStack.isItem(Tags.Items.TOOLS_SHEAR) ->
				shearLine(level, player, itemStack, lineUuid, selectedNode, hand)

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
		val maxLength = 10.0
		val maxLengthSquared = maxLength * maxLength
		val firstNode = itemStack.get(ModDataComponents.WEB_NODE)?.node

		if (firstNode == null) {
			storeFirstNode(player, itemStack, selectedNode)
			return
		}

		val bothAnchorsAreOnSameLine =
			firstNode is LineAnchor
				&& selectedNode is LineAnchor
				&& firstNode.lineUuid == selectedNode.lineUuid

		if (bothAnchorsAreOnSameLine) {
			player.status(ModMessageLang.SAME_LINE_MESSAGE.toComponent())
			return
		}

		val bothAnchorsFaceSameDirection =
			firstNode is BlockAnchor
				&& selectedNode is BlockAnchor
				&& firstNode.face == selectedNode.face

		if (bothAnchorsFaceSameDirection) {
			player.status(ModMessageLang.SAME_DIRECTION_MESSAGE.toComponent())
			return
		}

		if (firstNode.position.distanceToSqr(selectedNode.position) >= maxLengthSquared) {
			player.status(ModMessageLang.TOO_LONG_MESSAGE.toComponent())
			return
		}

		if (!hasLineOfSight(level, player, firstNode, selectedNode)) {
			player.status(ModMessageLang.OBSTRUCTED_MESSAGE.toComponent())
			return
		}

		createLine(level, player, itemStack, firstNode, selectedNode)
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

		level.playSound(
			null,
			selectedNode.position.x,
			selectedNode.position.y,
			selectedNode.position.z,
			SoundEvents.SHEEP_SHEAR,
			SoundSource.PLAYERS,
			1f, 1f
		)

		level.gameEvent(player, GameEvent.SHEAR, selectedNode.position)
	}

	private fun storeFirstNode(player: Player, itemStack: ItemStack, selectedNode: WebNode) {
		itemStack.set(ModDataComponents.WEB_NODE, WebNodeDataComponent(selectedNode))
		player.status(ModMessageLang.FIRST_NODE_MESSAGE.toComponent())
	}

	private fun createLine(
		level: ServerLevel,
		player: Player,
		itemStack: ItemStack,
		firstNode: WebNode,
		secondNode: WebNode
	) {
		val webLine = WebLine(UUID.randomUUID(), firstNode, secondNode)
		WebSavedData.get(level).addLine(level, webLine)
		itemStack.remove(ModDataComponents.WEB_NODE)
		player.status(ModMessageLang.LINE_CREATED_MESSAGE.toComponent())
	}

	fun getTargetedLineAnchor(
		lines: List<WebLine>,
		lookStart: Vec3,
		lookEnd: Vec3
	): LineAnchor? {
		val selectionRadiusSquared = LINE_SELECTION_RADIUS * LINE_SELECTION_RADIUS
		var targetedAnchor: LineAnchor? = null
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
			targetedAnchor = LineAnchor(
				line.uuid,
				Vec3(anchorPosition.x, anchorPosition.y, anchorPosition.z)
			)
		}

		return targetedAnchor
	}

	private fun hasLineOfSight(
		level: ServerLevel,
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

}