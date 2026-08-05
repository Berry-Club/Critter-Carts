package dev.aaronhowser.mods.critter_carts.entity.goal

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isBlock
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import dev.aaronhowser.mods.critter_carts.registry.ModBlocks
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.phys.Vec3
import java.util.*

class ScoochstemFollowGoal(
	private val scoochworm: ScoochwormEntity
) : Goal() {

	private var currentSurface: StemSurface? = null
	private var travelDirection: Direction? = null
	private var targetSurface: StemSurface? = null
	private var cornerPosition: Vec3? = null
	private var directionAfterCorner: Direction? = null

	init {
		flags = EnumSet.of(Flag.MOVE)
	}

	override fun canUse(): Boolean {
		if (!scoochworm.isMoving) return false

		val surface = findCurrentSurface() ?: return false
		val direction = getInitialTravelDirection(surface.bottom)
		val nextSurface = chooseNextSurface(surface, direction) ?: return false

		currentSurface = surface
		setTarget(surface, nextSurface, direction)
		return true
	}

	override fun canContinueToUse(): Boolean {
		return scoochworm.isMoving && targetSurface != null
	}

	override fun start() {
		scoochworm.isNoGravity = true
	}

	override fun tick() {
		val target = targetSurface ?: return
		val direction = travelDirection ?: return
		val targetPosition = cornerPosition ?: getEntityPosition(target)
		val distanceToTarget = scoochworm.position().vectorTo(targetPosition)
			.dot(Vec3.atLowerCornerOf(direction.normal))

		if (distanceToTarget <= TARGET_DISTANCE) {
			if (cornerPosition == null) {
				reachTarget(target, direction)
			} else {
				reachCorner(target, targetPosition)
			}
		}

		moveTowardTarget()
	}

	private fun reachTarget(target: StemSurface, direction: Direction) {
		scoochworm.setPos(getEntityPosition(target))
		scoochworm.attachmentBottom = target.bottom
		currentSurface = target

		val nextSurface = chooseNextSurface(target, direction)
		if (nextSurface == null) {
			targetSurface = null
			scoochworm.deltaMovement = Vec3.ZERO
			return
		}

		setTarget(target, nextSurface, direction)
	}

	private fun reachCorner(target: StemSurface, position: Vec3) {
		scoochworm.setPos(position)
		scoochworm.attachmentBottom = target.bottom
		cornerPosition = null
		travelDirection = directionAfterCorner
		directionAfterCorner = null
	}

	private fun setTarget(
		from: StemSurface,
		to: StemSurface,
		approachDirection: Direction
	) {
		targetSurface = to

		if (from.bottom == to.bottom) {
			cornerPosition = null
			directionAfterCorner = null
			travelDirection = directionTo(from, to, approachDirection)
			return
		}

		val fromPosition = getEntityPosition(from)
		val toPosition = getEntityPosition(to)
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
		cornerPosition = null
		directionAfterCorner = null
		scoochworm.noPhysics = false
		scoochworm.isNoGravity = !scoochworm.isMoving
	}

	private fun chooseNextSurface(surface: StemSurface, forward: Direction): StemSurface? {
		val forwardSurface = surface.copy(stem = surface.stem.relative(forward))
		if (isTraversableSurface(forwardSurface)) return forwardSurface

		val left = rotateAroundBottom(forward, surface.bottom, false)
		val right = rotateAroundBottom(forward, surface.bottom, true)

		val leftSurface = surface.copy(stem = surface.stem.relative(left))
		val rightSurface = surface.copy(stem = surface.stem.relative(right))

		val hasLeft = isTraversableSurface(leftSurface)
		val hasRight = isTraversableSurface(rightSurface)

		if (hasLeft || hasRight) {
			return when {
				hasLeft && hasRight -> if (scoochworm.random.nextBoolean()) leftSurface else rightSurface
				hasLeft -> leftSurface
				else -> rightSurface
			}
		}

		val forwardStem = surface.stem.relative(forward)
		val diagonalStem = forwardStem.relative(surface.bottom.opposite)
		val climbingSurface = StemSurface(
			diagonalStem,
			forward
		)

		if (
			isScoochstem(forwardStem)
			&& isTraversableSurface(climbingSurface)
		) {
			return climbingSurface
		}

		val adjoiningSurface = StemSurface(
			surface.stem.relative(surface.bottom.opposite),
			forward
		)

		if (isTraversableSurface(adjoiningSurface)) return adjoiningSurface

		val wrappingSurface = StemSurface(surface.stem, forward.opposite)
		if (
			isScoochstem(wrappingSurface.stem)
			&& hasNoCollision(forwardStem)
			&& hasNoCollision(diagonalStem)
		) {
			return wrappingSurface
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
			current.bottom != target.bottom,
			1.0
		)
	}

	private fun findCurrentSurface(): StemSurface? {
		for (bottom in Direction.entries) {
			val stem = BlockPos.containing(
				scoochworm.position()
					.add(Vec3.atLowerCornerOf(bottom.normal))
			)

			if (isScoochstem(stem)) return StemSurface(stem, bottom)
		}

		return null
	}

	private fun getInitialTravelDirection(bottom: Direction): Direction {
		val horizontal = Direction.fromYRot(scoochworm.yRot.toDouble())
		if (horizontal.axis != bottom.axis) return horizontal
		return if (bottom.axis == Direction.Axis.Y) Direction.NORTH else Direction.UP
	}

	private fun isTraversableSurface(surface: StemSurface): Boolean {
		if (!isScoochstem(surface.stem)) return false

		val position = getEntityPosition(surface)
		val bounds = scoochworm.boundingBox.move(
			position.x - scoochworm.x,
			position.y - scoochworm.y,
			position.z - scoochworm.z
		)

		return scoochworm.level().noCollision(scoochworm, bounds)
	}

	private fun isScoochstem(position: BlockPos): Boolean {
		return scoochworm.level()
			.getBlockState(position)
			.isBlock(ModBlocks.SCOOCHSTEM.get())
	}

	private fun hasNoCollision(position: BlockPos): Boolean {
		val level = scoochworm.level()
		return level.getBlockState(position)
			.getCollisionShape(level, position)
			.isEmpty
	}

	private fun getEntityPosition(surface: StemSurface): Vec3 {
		val blockCenter = Vec3.atCenterOf(surface.stem)
		val center = blockCenter.subtract(
			Vec3.atLowerCornerOf(surface.bottom.normal)
				.scale(0.5 + ScoochwormEntity.SIZE / 2.0)
		)

		return center.subtract(0.0, ScoochwormEntity.SIZE / 2.0, 0.0)
	}

	private fun directionTo(
		from: StemSurface,
		to: StemSurface,
		previousDirection: Direction
	): Direction {
		if (from.bottom != to.bottom) {
			return if (to.bottom == previousDirection) {
				from.bottom.opposite
			} else {
				from.bottom
			}
		}

		val displacement = getEntityPosition(from).vectorTo(getEntityPosition(to))
		return Direction.getNearest(displacement.x, displacement.y, displacement.z)
	}

	private fun directionBetween(from: Vec3, to: Vec3): Direction {
		val displacement = from.vectorTo(to)
		return Direction.getNearest(displacement.x, displacement.y, displacement.z)
	}

	private fun rotateAroundBottom(
		direction: Direction,
		bottom: Direction,
		clockwise: Boolean
	): Direction {
		val first = if (clockwise) bottom.normal else direction.normal
		val second = if (clockwise) direction.normal else bottom.normal

		return Direction.fromDelta(
			first.y * second.z - first.z * second.y,
			first.z * second.x - first.x * second.z,
			first.x * second.y - first.y * second.x
		) ?: direction
	}

	companion object {
		private const val TARGET_DISTANCE = 0.001
	}
}