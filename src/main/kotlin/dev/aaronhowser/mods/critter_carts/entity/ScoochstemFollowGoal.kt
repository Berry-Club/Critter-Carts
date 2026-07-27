package dev.aaronhowser.mods.critter_carts.entity

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

		travelDirection = directionTo(currentStem, nextStem)
		targetStem = nextStem
		return true
	}

	override fun canContinueToUse(): Boolean = targetStem != null

	override fun tick() {
		val target = targetStem ?: return
		val targetX = target.x + 0.5
		val targetY = target.y + 1.0
		val targetZ = target.z + 0.5

		scoochworm.moveControl.setWantedPosition(targetX, targetY, targetZ, 1.0)

		val xDistance = scoochworm.x - targetX
		val zDistance = scoochworm.z - targetZ
		val horizontalDistance = xDistance * xDistance + zDistance * zDistance
		if (horizontalDistance > TARGET_DISTANCE_SQUARED) return

		scoochworm.setPos(targetX, targetY, targetZ)
		val direction = travelDirection ?: return
		val nextStem = chooseNextStem(target, direction)

		if (nextStem == null) {
			targetStem = null
			scoochworm.deltaMovement = scoochworm.deltaMovement.multiply(0.0, 1.0, 0.0)
			return
		}

		travelDirection = directionTo(target, nextStem)
		targetStem = nextStem
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

	private fun isScoochstem(position: BlockPos): Boolean {
		return scoochworm.level().getBlockState(position).`is`(ModBlocks.SCOOCHSTEM.get())
	}

	private fun directionTo(from: BlockPos, to: BlockPos): Direction {
		return Direction.fromDelta(to.x - from.x, 0, to.z - from.z)
			?: error("Scoochstem path positions must be horizontally adjacent")
	}

	companion object {
		private const val TARGET_DISTANCE_SQUARED = 0.01
	}
}