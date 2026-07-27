package dev.aaronhowser.mods.critter_carts.entity.goal

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isBlock
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import dev.aaronhowser.mods.critter_carts.registry.ModBlocks
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.ai.goal.Goal
import java.util.*

class ScoochstemFollowGoal(
	private val scoochworm: ScoochwormEntity
) : Goal() {

	private var travelDirection: Direction? = null
	private var targetStem: BlockPos? = null

	init {
		flags = EnumSet.of(Flag.MOVE)
	}

	override fun canUse(): Boolean {
		val currentStem = scoochworm.blockPosition().below()
		if (!isScoochstem(currentStem)) return false

		val currentDirection = travelDirection ?: Direction.fromYRot(scoochworm.yRot.toDouble())
		val nextStem = chooseNextStem(currentStem, currentDirection) ?: return false

		val nextDirection = directionTo(currentStem, nextStem)
		travelDirection = nextDirection
		targetStem = findSegmentEnd(currentStem, nextDirection)
		return true
	}

	override fun canContinueToUse(): Boolean = targetStem != null

	override fun tick() {
		val currentTargetStem = targetStem ?: return
		val currentDirection = travelDirection ?: return

		val targetX = currentTargetStem.x + 0.5
		val targetZ = currentTargetStem.z + 0.5

		val distanceToTarget = (targetX - scoochworm.x) * currentDirection.stepX +
			(targetZ - scoochworm.z) * currentDirection.stepZ

		val hasReachedTarget = distanceToTarget <= TARGET_DISTANCE
		if (hasReachedTarget) {
			reachTarget(currentTargetStem, currentDirection)
		}

		moveTowardTarget()
	}

	private fun reachTarget(currentTargetStem: BlockPos, currentDirection: Direction) {
		scoochworm.setPos(
			currentTargetStem.x + 0.5,
			scoochworm.y,
			currentTargetStem.z + 0.5
		)

		val nextStem = chooseNextStem(currentTargetStem, currentDirection)
		if (nextStem == null) {
			targetStem = null
			scoochworm.deltaMovement = scoochworm
				.deltaMovement
				.multiply(0.0, 1.0, 0.0)

			return
		}

		val nextDirection = directionTo(currentTargetStem, nextStem)
		travelDirection = nextDirection
		targetStem = findSegmentEnd(currentTargetStem, nextDirection)
	}

	override fun stop() {
		targetStem = null
	}

	private fun chooseNextStem(currentStem: BlockPos, forwardDirection: Direction): BlockPos? {
		val forwardStem = currentStem.relative(forwardDirection)
		if (isTraversableStem(currentStem, forwardStem)) return forwardStem

		val leftStem = currentStem.relative(forwardDirection.counterClockWise)
		val hasLeftStem = isTraversableStem(currentStem, leftStem)

		val rightStem = currentStem.relative(forwardDirection.clockWise)
		val hasRightStem = isTraversableStem(currentStem, rightStem)

		if (hasLeftStem && hasRightStem) {
			return if (scoochworm.random.nextBoolean()) leftStem else rightStem
		}

		if (hasLeftStem) return leftStem
		if (hasRightStem) return rightStem
		return null
	}

	private fun findSegmentEnd(start: BlockPos, direction: Direction): BlockPos {
		var segmentEnd = start.relative(direction)
		var nextStem = segmentEnd.relative(direction)

		while (isTraversableStem(segmentEnd, nextStem)) {
			segmentEnd = nextStem
			nextStem = segmentEnd.relative(direction)
		}

		return segmentEnd
	}

	private fun moveTowardTarget() {
		val currentTargetStem = targetStem ?: return
		val currentDirection = travelDirection ?: return

		scoochworm.scoochwormMoveControl.setWantedPosition(
			currentTargetStem.x + 0.5,
			currentTargetStem.y + 1.0,
			currentTargetStem.z + 0.5,
			currentDirection,
			1.0
		)
	}

	private fun isScoochstem(position: BlockPos): Boolean {
		return scoochworm.level()
			.getBlockState(position)
			.isBlock(ModBlocks.SCOOCHSTEM.get())
	}

	private fun isTraversableStem(from: BlockPos, to: BlockPos): Boolean {
		if (!isScoochstem(to)) return false

		val fromX = from.x + 0.5
		val fromY = from.y + 1.0
		val fromZ = from.z + 0.5

		val toX = to.x + 0.5
		val toY = to.y + 1.0
		val toZ = to.z + 0.5

		val fromBounds = scoochworm.boundingBox.move(
			fromX - scoochworm.x,
			fromY - scoochworm.y,
			fromZ - scoochworm.z
		)

		val travelBounds = fromBounds.expandTowards(
			toX - fromX,
			toY - fromY,
			toZ - fromZ
		)

		return scoochworm.level().noCollision(scoochworm, travelBounds)
	}

	private fun directionTo(from: BlockPos, to: BlockPos): Direction {
		return Direction.fromDelta(to.x - from.x, 0, to.z - from.z)
			?: error("Scoochstem path positions must be horizontally adjacent")
	}

	companion object {
		private const val TARGET_DISTANCE = 0.1
	}

}