package dev.aaronhowser.mods.critter_carts.entity.goal

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.toVec3
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.phys.Vec3
import java.util.*

class ScoochwormTravelGoal(
	private val scoochworm: ScoochwormEntity
) : Goal() {

	private var currentSurface: ScoochwormSupport? = null
	private var travelDirection: Direction? = null
	private var targetSurface: ScoochwormSupport? = null
	private var cornerPosition: Vec3? = null
	private var directionAfterCorner: Direction? = null

	init {
		flags = EnumSet.of(Flag.MOVE)
	}

	override fun canUse(): Boolean {
		if (!scoochworm.isTryingToMove) return false

		val surface = findCurrentSurface() ?: return false
		scoochworm.attachToSupport(surface.supportPosition, surface.supportDirection)

		val direction = getTravelDirection(surface.supportDirection)
		val nextSurface = chooseNextSurface(surface, direction) ?: return false

		currentSurface = surface
		setTarget(surface, nextSurface, direction)
		return true
	}

	override fun canContinueToUse(): Boolean {
		return scoochworm.isTryingToMove && targetSurface != null
	}

	override fun start() {
		scoochworm.isNoGravity = true
	}

	override fun tick() {
		val target = targetSurface ?: return
		val direction = travelDirection ?: return

		val targetPosition = cornerPosition ?: getEntityPosition(target)
		val displacement = scoochworm.position().vectorTo(targetPosition)
		// Only check how far the worm has left to move along its current path. Sideways
		// distance does not matter because the worm moves around a corner one step at a time.
		val distanceToTarget = displacement
			.dot(direction.normal.toVec3())

		val targetDistance = 0.001
		if (distanceToTarget <= targetDistance) {
			if (cornerPosition == null) {
				reachTarget(target, direction)
			} else {
				reachCorner(target, targetPosition)
			}
		}

		moveTowardTarget()
	}

	private fun reachTarget(target: ScoochwormSupport, direction: Direction) {
		scoochworm.setPos(getEntityPosition(target))
		scoochworm.attachToSupport(target.supportPosition, target.supportDirection)
		currentSurface = target

		val nextSurface = chooseNextSurface(target, direction)
		if (nextSurface == null) {
			targetSurface = null
			scoochworm.deltaMovement = Vec3.ZERO
			return
		}

		setTarget(target, nextSurface, direction)
	}

	private fun reachCorner(target: ScoochwormSupport, position: Vec3) {
		scoochworm.setPos(position)
		scoochworm.attachToSupport(target.supportPosition, target.supportDirection)
		cornerPosition = null
		travelDirection = directionAfterCorner
		directionAfterCorner = null
	}

	private fun setTarget(
		from: ScoochwormSupport,
		to: ScoochwormSupport,
		approachDirection: Direction
	) {
		targetSurface = to

		if (from.supportDirection == to.supportDirection) {
			cornerPosition = null
			directionAfterCorner = null
			travelDirection = directionTo(from, to, approachDirection)
			return
		}

		val fromPosition = getEntityPosition(from)
		val toPosition = getEntityPosition(to)

		// When turning upwards or downwards, first move to the corner where the two paths
		// meet. From there, move onto the new side. This stops the worm from taking a
		// diagonal shortcut through the blocks.
		val corner = when (approachDirection.axis) {
			Direction.Axis.X -> Vec3(toPosition.x, fromPosition.y, fromPosition.z)
			Direction.Axis.Y -> Vec3(fromPosition.x, toPosition.y, fromPosition.z)
			Direction.Axis.Z -> Vec3(fromPosition.x, fromPosition.y, toPosition.z)
		}

		cornerPosition = corner
		directionAfterCorner = directionBetween(corner, toPosition)
		travelDirection = approachDirection
	}

	override fun stop() {
		targetSurface = null
		currentSurface = null
		travelDirection = null
		cornerPosition = null
		directionAfterCorner = null
		scoochworm.noPhysics = false
	}

	private fun chooseNextSurface(surface: ScoochwormSupport, forward: Direction): ScoochwormSupport? {
		val supportPosition = surface.supportPosition
		val supportDirection = surface.supportDirection

		val forwardPosition = supportPosition.relative(forward)
		val forwardSurface = surface.copy(supportPosition = forwardPosition)

		if (isTraversableSurface(forwardSurface)) return forwardSurface

		val left = rotateAroundSupport(forward, supportDirection, false)
		val right = rotateAroundSupport(forward, supportDirection, true)

		val leftPosition = supportPosition.relative(left)
		val rightPosition = supportPosition.relative(right)

		val leftSurface = surface.copy(supportPosition = leftPosition)
		val rightSurface = surface.copy(supportPosition = rightPosition)

		val hasLeft = isTraversableSurface(leftSurface)
		val hasRight = isTraversableSurface(rightSurface)

		if (hasLeft || hasRight) {
			return when {
				hasLeft && hasRight -> if (scoochworm.random.nextBoolean()) leftSurface else rightSurface
				hasLeft -> leftSurface
				else -> rightSurface
			}
		}

		// There is no stem to move to ahead, left, or right on this side. First try to
		// turn upwards onto the side in front of the worm.
		val upperForwardPosition = forwardPosition.relative(supportDirection.opposite)
		val upwardTurnSurface = ScoochwormSupport(
			upperForwardPosition,
			forward
		)

		if (
			isTravelSurface(forwardPosition, supportDirection)
			&& isTraversableSurface(upwardTurnSurface)
		) {
			return upwardTurnSurface
		}

		val nearbyUpwardTurnSurface = ScoochwormSupport(
			supportPosition.relative(supportDirection.opposite),
			forward
		)

		if (isTraversableSurface(nearbyUpwardTurnSurface)) return nearbyUpwardTurnSurface

		val downwardTurnSurface = ScoochwormSupport(supportPosition, forward.opposite)
		// If the worm cannot turn upwards, try turning downwards around the edge. It passes
		// through both blocks in front during this turn, so both blocks need to be clear.
		if (
			isTravelSurface(downwardTurnSurface)
			&& hasNoCollision(forwardPosition)
			&& hasNoCollision(upperForwardPosition)
		) {
			return downwardTurnSurface
		}

		return null
	}

	private fun moveTowardTarget() {
		val current = currentSurface ?: return
		val target = targetSurface ?: return
		val direction = travelDirection ?: return
		val position = cornerPosition ?: getEntityPosition(target)

		scoochworm.scoochwormMoveControl.setWantedPosition(
			position.x,
			position.y,
			position.z,
			direction,
			current.supportDirection != target.supportDirection,
			1.0
		)
	}

	private fun findCurrentSurface(): ScoochwormSupport? {
		for (supportDirection in Direction.entries) {
			val stem = BlockPos.containing(
				scoochworm.position()
					.add(supportDirection.normal.toVec3())
			)

			if (isTravelSurface(stem, supportDirection)) {
				return ScoochwormSupport(stem, supportDirection)
			}
		}

		return null
	}

	private fun getInitialTravelDirection(supportDirection: Direction): Direction {
		val surfaceDirection = scoochworm.surfaceTravelDirection
		if (surfaceDirection != null) {
			scoochworm.surfaceTravelDirection = null
			return Direction.entries
				.filter { it.axis != supportDirection.axis }
				.maxBy { direction ->
					surfaceDirection.dot(direction.normal.toVec3())
				}
		}

		val horizontal = Direction.fromYRot(scoochworm.yRot.toDouble())
		if (horizontal.axis != supportDirection.axis) return horizontal
		return if (supportDirection.axis == Direction.Axis.Y) Direction.NORTH else Direction.UP
	}

	private fun getTravelDirection(supportDirection: Direction): Direction {
		val currentDirection = travelDirection
		if (
			currentDirection != null
			&& currentDirection.axis != supportDirection.axis
		) {
			return currentDirection
		}

		return getInitialTravelDirection(supportDirection)
	}

	private fun isTraversableSurface(surface: ScoochwormSupport): Boolean {
		if (!isTravelSurface(surface)) return false

		val position = getEntityPosition(surface)
		// Move a copy of the worm's hitbox to the target and make sure it fits there.
		val bounds = scoochworm.boundingBox.move(
			position.x - scoochworm.x,
			position.y - scoochworm.y,
			position.z - scoochworm.z
		)

		return scoochworm.level().noCollision(scoochworm, bounds)
	}

	private fun isTravelSurface(surface: ScoochwormSupport): Boolean {
		return isTravelSurface(surface.supportPosition, surface.supportDirection)
	}

	private fun isTravelSurface(position: BlockPos, supportDirection: Direction): Boolean {
		return ScoochwormEntity.supportsScoochwormTravel(
			scoochworm.level(),
			position,
			supportDirection.opposite
		)
	}

	private fun hasNoCollision(position: BlockPos): Boolean {
		val level = scoochworm.level()
		return level.getBlockState(position)
			.getCollisionShape(level, position)
			.isEmpty
	}

	private fun getEntityPosition(surface: ScoochwormSupport): Vec3 {
		val blockCenter = Vec3.atCenterOf(surface.supportPosition)
		val center = blockCenter.subtract(
			surface.supportDirection.normal
				.toVec3()
				.scale(0.5 + ScoochwormEntity.SIZE / 2.0)
		)

		return center.subtract(0.0, ScoochwormEntity.SIZE / 2.0, 0.0)
	}

	private fun directionTo(
		from: ScoochwormSupport,
		to: ScoochwormSupport,
		previousDirection: Direction
	): Direction {
		if (from.supportDirection != to.supportDirection) {
			return if (to.supportDirection == previousDirection) {
				from.supportDirection.opposite
			} else {
				from.supportDirection
			}
		}

		val displacement = getEntityPosition(from).vectorTo(getEntityPosition(to))
		return Direction.getNearest(displacement.x, displacement.y, displacement.z)
	}

	private fun directionBetween(from: Vec3, to: Vec3): Direction {
		val displacement = from.vectorTo(to)
		return Direction.getNearest(displacement.x, displacement.y, displacement.z)
	}

	private fun rotateAroundSupport(
		direction: Direction,
		supportDirection: Direction,
		clockwise: Boolean
	): Direction {
		val first = if (clockwise) supportDirection.normal else direction.normal
		val second = if (clockwise) direction.normal else supportDirection.normal

		// Use the path and the side holding the worm to find left or right. This works the
		// same way on a floor, wall, or ceiling.
		return Direction.fromDelta(
			first.y * second.z - first.z * second.y,
			first.z * second.x - first.x * second.z,
			first.x * second.y - first.y * second.x
		) ?: direction
	}

}