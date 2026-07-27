package dev.aaronhowser.mods.critter_carts.entity.goal

import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import dev.aaronhowser.mods.critter_carts.registry.ModBlocks
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.Goal
import java.util.EnumSet
import kotlin.math.abs
import kotlin.math.min

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
		val direction = travelDirection ?: return

		val targetX = target.x + 0.5
		val targetZ = target.z + 0.5

		val distance = if (direction.axis == Direction.Axis.X) {
			abs(targetX - scoochworm.x)
		} else {
			abs(targetZ - scoochworm.z)
		}

		if (distance > TARGET_DISTANCE) {
			moveToward(targetX, targetZ, direction, distance)
			return
		}

		scoochworm.setPos(targetX, scoochworm.y, targetZ)
		scoochworm.deltaMovement = scoochworm.deltaMovement.multiply(0.0, 1.0, 0.0)
		val nextStem = chooseNextStem(target, direction)

		if (nextStem == null) {
			targetStem = null
			return
		}

		travelDirection = directionTo(target, nextStem)
		targetStem = nextStem
	}

	private fun moveToward(targetX: Double, targetZ: Double, direction: Direction, distance: Double) {
		val movementSpeed = scoochworm.getAttributeValue(Attributes.MOVEMENT_SPEED) * MOVEMENT_SPEED_SCALE
		val movement = min(movementSpeed, distance)

		val nextX = if (direction.axis == Direction.Axis.X) {
			scoochworm.x + direction.stepX * movement
		} else {
			targetX
		}

		val nextZ = if (direction.axis == Direction.Axis.Z) {
			scoochworm.z + direction.stepZ * movement
		} else {
			targetZ
		}

		scoochworm.setPos(nextX, scoochworm.y, nextZ)
		scoochworm.deltaMovement = scoochworm.deltaMovement.multiply(0.0, 1.0, 0.0)
		scoochworm.yRot = direction.toYRot()
		scoochworm.yBodyRot = scoochworm.yRot
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
		private const val MOVEMENT_SPEED_SCALE = 0.1
		private const val TARGET_DISTANCE = 0.001
	}

}