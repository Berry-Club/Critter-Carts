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
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.common.Tags
import java.util.UUID

object WebLineInteractionHandler {

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
		val lookEnd = eyePosition.add(player.lookAngle.scale(player.blockInteractionRange()))
		val lineAnchor = findLineAnchor(listOf(line), eyePosition, lookEnd) ?: return
		val positionTolerance = 0.1
		val positionToleranceSquared = positionTolerance * positionTolerance

		if (lineAnchor.position.distanceToSqr(requestedPosition) > positionToleranceSquared) return
		if (itemStack.isItem(Tags.Items.TOOLS_SHEAR)) {
			WebSavedData.get(level).removeLine(level, lineUuid)
			player.playSound(SoundEvents.SHEEP_SHEAR, 1f, 1f)
			level.gameEvent(player, GameEvent.SHEAR, lineAnchor.position)
			itemStack.hurtAndBreak(1, player, hand.getEquipmentSlot())
			player.status(ModMessageLang.LINE_REMOVED_MESSAGE.toComponent())
			return
		}

		handleAnchor(level, player, itemStack, lineAnchor)
	}

	fun handleAnchor(
		level: ServerLevel,
		player: Player,
		itemStack: ItemStack,
		selectedAnchor: WebNode
	) {
		val maxLength = 10.0
		val maxLengthSquared = maxLength * maxLength
		val storedAnchor = itemStack.get(ModDataComponents.WEB_NODE)?.node

		if (storedAnchor == null) {
			itemStack.set(ModDataComponents.WEB_NODE, WebNodeDataComponent(selectedAnchor))
			player.status(ModMessageLang.FIRST_NODE_MESSAGE.toComponent())
			return
		}

		if (storedAnchor is LineAnchor
			&& selectedAnchor is LineAnchor
			&& storedAnchor.lineUuid == selectedAnchor.lineUuid
		) {
			player.status(ModMessageLang.SAME_LINE_MESSAGE.toComponent())
			return
		}

		if (storedAnchor is BlockAnchor
			&& selectedAnchor is BlockAnchor
			&& storedAnchor.face == selectedAnchor.face
		) {
			player.status(ModMessageLang.SAME_DIRECTION_MESSAGE.toComponent())
			return
		}

		if (storedAnchor.position.distanceToSqr(selectedAnchor.position) >= maxLengthSquared) {
			player.status(ModMessageLang.TOO_LONG_MESSAGE.toComponent())
			return
		}

		if (!hasLineOfSight(level, player, storedAnchor, selectedAnchor)) {
			player.status(ModMessageLang.OBSTRUCTED_MESSAGE.toComponent())
			return
		}

		val line = WebLine(UUID.randomUUID(), storedAnchor, selectedAnchor)
		WebSavedData.get(level).addLine(level, line)
		itemStack.remove(ModDataComponents.WEB_NODE)
		player.status(ModMessageLang.LINE_CREATED_MESSAGE.toComponent())
	}

	fun findLineAnchor(
		lines: Collection<WebLine>,
		lookStart: Vec3,
		lookEnd: Vec3
	): LineAnchor? {
		val selectionRadius = 0.3
		val selectionRadiusSquared = selectionRadius * selectionRadius
		var lineAnchor: LineAnchor? = null
		var closestDistanceSquared = selectionRadiusSquared

		for (line in lines) {
			val position = getClosestPosition(line, lookStart, lookEnd)
			val lookPosition = getClosestPosition(lookStart, lookEnd, position)
			val distanceSquared = position.distanceToSqr(lookPosition)
			if (distanceSquared > closestDistanceSquared) continue

			closestDistanceSquared = distanceSquared
			lineAnchor = LineAnchor(line.uuid, position)
		}

		return lineAnchor
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

	private fun getClosestPosition(line: WebLine, lookStart: Vec3, lookEnd: Vec3): Vec3 {
		val parallelTolerance = 1.0e-7
		val lineStart = line.firstNode.position
		val lineDirection = line.secondNode.position.subtract(lineStart)
		val lookDirection = lookEnd.subtract(lookStart)
		val offset = lineStart.subtract(lookStart)
		val lineLengthSquared = lineDirection.lengthSqr()
		val lookLengthSquared = lookDirection.lengthSqr()
		val directionsDot = lineDirection.dot(lookDirection)
		val denominator = lineLengthSquared * lookLengthSquared - directionsDot * directionsDot

		if (denominator <= parallelTolerance) {
			return getClosestPosition(lineStart, line.secondNode.position, lookStart)
		}

		val numerator = directionsDot * lookDirection.dot(offset) -
			lookLengthSquared * lineDirection.dot(offset)
		val lineParameter = numerator / denominator
		return lineStart.add(lineDirection.scale(lineParameter.coerceIn(0.0, 1.0)))
	}

	private fun getClosestPosition(start: Vec3, end: Vec3, position: Vec3): Vec3 {
		val direction = end.subtract(start)
		val lengthSquared = direction.lengthSqr()
		if (lengthSquared == 0.0) return start

		val parameter = position.subtract(start).dot(direction) / lengthSquared
		return start.add(direction.scale(parameter.coerceIn(0.0, 1.0)))
	}
}