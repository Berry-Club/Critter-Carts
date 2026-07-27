package dev.aaronhowser.mods.critter_carts.entity.goal

import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import dev.aaronhowser.mods.critter_carts.registry.ModBlocks
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.ai.goal.Goal
import java.util.EnumSet

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

		val direction = travelDirection ?: Direction.fromYRot(scoochworm.yRot.toDouble())
		val nextStem = chooseNextStem(currentStem, direction) ?: return false

		val nextDirection = directionTo(currentStem, nextStem)
		travelDirection = nextDirection
		targetStem = findSegmentEnd(currentStem, nextDirection)
		return true
	}

	override fun canContinueToUse(): Boolean = targetStem != null

	override fun tick() {
		val target = targetStem ?: return
		val direction = travelDirection ?: return

		val targetX = target.x + 0.5
		val targetZ = target.z + 0.5

		val distance = (targetX - scoochworm.x) * direction.stepX +
			(targetZ - scoochworm.z) * direction.stepZ

		if (distance <= TARGET_DISTANCE) {
			scoochworm.setPos(targetX, scoochworm.y, targetZ)
			val nextStem = chooseNextStem(target, direction)

			if (nextStem == null) {
				targetStem = null
				scoochworm.deltaMovement = scoochworm.deltaMovement.multiply(0.0, 1.0, 0.0)
				return
			}

			val nextDirection = directionTo(target, nextStem)
			travelDirection = nextDirection
			targetStem = findSegmentEnd(target, nextDirection)
		}

		moveTowardTarget()
	}

	override fun stop() {
		targetStem = null
	}

	private fun chooseNextStem(currentStem: BlockPos, forward: Direction): BlockPos? {
		val forwardStem = currentStem.relative(forward)
		if (isScoochstem(forwardStem)) return forwardStem

		val left = forward.counterClockWise
		val right = forward.clockWise
		val leftStem = currentStem.relative(left)
		val rightStem = currentStem.relative(right)
		val hasLeftStem = isScoochstem(leftStem)
		val hasRightStem = isScoochstem(rightStem)

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

		while (isScoochstem(nextStem)) {
			segmentEnd = nextStem
			nextStem = segmentEnd.relative(direction)
		}

		return segmentEnd
	}

	private fun moveTowardTarget() {
		val target = targetStem ?: return
		val direction = travelDirection ?: return
		scoochworm.scoochwormMoveControl.setWantedPosition(
			target.x + 0.5,
			target.y + 1.0,
			target.z + 0.5,
			direction,
			1.0
		)
	}

	private fun isScoochstem(position: BlockPos): Boolean {
		return scoochworm.level().getBlockState(position).`is`(ModBlocks.SCOOCHSTEM.get())
	}

	private fun directionTo(from: BlockPos, to: BlockPos): Direction {
		return Direction.fromDelta(to.x - from.x, 0, to.z - from.z)
			?: error("Scoochstem path positions must be horizontally adjacent")
	}

	companion object {
		private const val TARGET_DISTANCE = 0.1
	}

}