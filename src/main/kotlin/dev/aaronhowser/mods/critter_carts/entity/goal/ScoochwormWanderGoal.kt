package dev.aaronhowser.mods.critter_carts.entity.goal

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.toVec3
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.Mth
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.phys.Vec3
import java.util.*
import kotlin.math.abs

class ScoochwormWanderGoal(
	private val scoochworm: ScoochwormEntity
) : Goal() {

	private var currentSupport: ScoochwormSupport? = null
	private var movementDirection = Vec3.ZERO
	private var nextTurnTick = 0
	private var transitionTicks = 0

	init {
		flags = EnumSet.of(Flag.MOVE)
	}

	override fun canUse(): Boolean {
		if (!scoochworm.isTryingToMove) return false

		val currentSupport = findCurrentSupport() ?: return false
		if (isStem(currentSupport)) return false

		this.currentSupport = currentSupport
		movementDirection = getInitialDirection(currentSupport.supportDirection)
		scoochworm.attachToSupport(
			currentSupport.supportPosition,
			currentSupport.supportDirection
		)

		return true
	}

	override fun canContinueToUse(): Boolean {
		val currentSupport = this.currentSupport ?: return false
		return scoochworm.isTryingToMove && !isStem(currentSupport)
	}

	override fun start() {
		scoochworm.isNoGravity = true
		scoochworm.noPhysics = false
		nextTurnTick = scoochworm.tickCount + TURN_INTERVAL_TICKS
	}

	override fun tick() {
		val currentSupport = this.currentSupport ?: return

		tryTurning(currentSupport)

		val movementSpeed = scoochworm.getAttributeValue(Attributes.MOVEMENT_SPEED)
		val movement = movementDirection.scale(movementSpeed)
		val nextPosition = scoochworm.position().add(movement)

		if (transitionTicks > 0) {
			continueTransition(currentSupport, nextPosition, movement)
			return
		}

		if (tryMoveForward(currentSupport, nextPosition, movement)) return

		val edgeDirection = getCrossedDirection(currentSupport, nextPosition)
		if (edgeDirection == null) {
			move(movement)
			return
		}

		if (tryTurnAlongSurface(currentSupport, edgeDirection)) return
		if (tryTurnUpwards(currentSupport, edgeDirection)) return
		if (tryTurnDownwards(currentSupport, edgeDirection)) return

		movementDirection = movementDirection.scale(-1.0)
		move(Vec3.ZERO)
	}

	override fun stop() {
		scoochworm.noPhysics = false
		scoochworm.isTurningAroundCorner = false
		transitionTicks = 0
		currentSupport = null
	}

	private fun turn(supportDirection: Direction) {
		val maximumTurnRadians = Math.PI / 6.0
		val angle = scoochworm.random.nextDouble() * maximumTurnRadians
		val signedAngle = if (scoochworm.random.nextBoolean()) angle else -angle

		movementDirection = rotateAroundAxis(
			movementDirection,
			supportDirection.normal.toVec3(),
			signedAngle
		).normalize()
	}

	private fun tryTurning(currentSupport: ScoochwormSupport) {
		if (transitionTicks > 0 || scoochworm.tickCount < nextTurnTick) return

		turn(currentSupport.supportDirection)
		nextTurnTick = scoochworm.tickCount + TURN_INTERVAL_TICKS
	}

	private fun tryMoveForward(
		currentSupport: ScoochwormSupport,
		nextPosition: Vec3,
		movement: Vec3
	): Boolean {
		val nextSupport = getSupportAt(nextPosition, currentSupport.supportDirection)
		if (nextSupport == null) return false

		moveOntoSupport(nextSupport, movement)
		return true
	}

	private fun tryTurnAlongSurface(
		support: ScoochwormSupport,
		forward: Direction
	): Boolean {
		val left = turnAlongSurface(forward, support.supportDirection, false)
		val right = turnAlongSurface(forward, support.supportDirection, true)
		val leftSupport = support.copy(
			supportPosition = support.supportPosition.relative(left)
		)
		val rightSupport = support.copy(
			supportPosition = support.supportPosition.relative(right)
		)
		val canTurnLeft = isAnySurface(leftSupport)
		val canTurnRight = isAnySurface(rightSupport)

		val turnDirection = when {
			canTurnLeft && canTurnRight -> if (scoochworm.random.nextBoolean()) left else right
			canTurnLeft -> left
			canTurnRight -> right
			else -> return false
		}

		movementDirection = turnDirection.normal.toVec3()
		move(Vec3.ZERO)
		return true
	}

	private fun tryTurnUpwards(
		support: ScoochwormSupport,
		forward: Direction
	): Boolean {
		val upperForwardPosition = support.supportPosition
			.relative(forward)
			.relative(support.supportDirection.opposite)
		val upwardTurnSupport = ScoochwormSupport(upperForwardPosition, forward)

		if (isAnySurface(upwardTurnSupport)) {
			transitionTo(upwardTurnSupport, support.supportDirection.opposite, false)
			return true
		}

		val nearbyUpwardTurnSupport = ScoochwormSupport(
			support.supportPosition.relative(support.supportDirection.opposite),
			forward
		)

		if (!isAnySurface(nearbyUpwardTurnSupport)) return false

		transitionTo(nearbyUpwardTurnSupport, support.supportDirection.opposite, false)
		return true
	}

	private fun tryTurnDownwards(
		support: ScoochwormSupport,
		forward: Direction
	): Boolean {
		val downwardTurnSupport = ScoochwormSupport(
			support.supportPosition,
			forward.opposite
		)

		if (!isAnySurface(downwardTurnSupport)) return false

		val forwardPosition = support.supportPosition.relative(forward)
		if (!hasNoCollision(forwardPosition)) return false

		val upperForwardPosition = forwardPosition.relative(support.supportDirection.opposite)
		if (!hasNoCollision(upperForwardPosition)) return false

		transitionTo(downwardTurnSupport, support.supportDirection, true)

		return true
	}

	private fun moveOntoSupport(nextSupport: ScoochwormSupport, movement: Vec3) {
		if (isStem(nextSupport)) {
			handOffToStem(nextSupport)
			return
		}

		currentSupport = nextSupport
		scoochworm.attachToSupport(nextSupport.supportPosition, nextSupport.supportDirection)
		move(movement)
	}

	private fun transitionTo(
		newSupport: ScoochwormSupport,
		newDirection: Direction,
		snapToEntry: Boolean
	) {
		currentSupport = newSupport
		movementDirection = curveDirectionOntoSurface(newSupport, newDirection)
		transitionTicks = TRANSITION_TICKS
		scoochworm.noPhysics = true
		scoochworm.isTurningAroundCorner = true
		scoochworm.attachToSupport(newSupport.supportPosition, newSupport.supportDirection)
		snapToSurface(newSupport)
		if (snapToEntry) {
			snapToEntryEdge(newSupport, newDirection)
		}
		move(Vec3.ZERO)
	}

	private fun curveDirectionOntoSurface(
		newSupport: ScoochwormSupport,
		newDirection: Direction
	): Vec3 {
		val newSurfaceNormal = newSupport.supportDirection.normal.toVec3()

		val directionAlongCorner = movementDirection.subtract(
			newSurfaceNormal.scale(movementDirection.dot(newSurfaceNormal))
		)

		// Preserve the part of the old motion aimed into the corner, but redirect it onto
		// the new surface so the turn keeps the same general speed and heading.
		val directionIntoCorner = abs(movementDirection.dot(newSurfaceNormal))

		val directionAroundCorner = newDirection.normal.toVec3()
			.scale(directionIntoCorner)

		return directionAlongCorner.add(directionAroundCorner).normalize()
	}

	private fun continueTransition(
		currentSupport: ScoochwormSupport,
		nextPosition: Vec3,
		movement: Vec3
	) {
		val nextSupport = getSupportAt(nextPosition, currentSupport.supportDirection)
		if (nextSupport != null) {
			if (isStem(nextSupport)) {
				handOffToStem(nextSupport)
				transitionTicks = 0
				scoochworm.noPhysics = false
				scoochworm.isTurningAroundCorner = false
				return
			}

			this.currentSupport = nextSupport
			scoochworm.attachToSupport(nextSupport.supportPosition, nextSupport.supportDirection)
		}

		move(movement)
		transitionTicks--
		if (transitionTicks == 0) {
			scoochworm.noPhysics = false
			scoochworm.isTurningAroundCorner = false
		}
	}

	private fun handOffToStem(stemSupport: ScoochwormSupport) {
		currentSupport = stemSupport
		scoochworm.rememberedMovementDirection = movementDirection
		scoochworm.attachToSupport(stemSupport.supportPosition, stemSupport.supportDirection)
		snapToSurface(stemSupport)
		scoochworm.deltaMovement = Vec3.ZERO
	}

	private fun move(movement: Vec3) {
		scoochworm.deltaMovement = movement
		scoochworm.yRot = Mth.atan2(-movementDirection.x, movementDirection.z)
			.times(Mth.RAD_TO_DEG)
			.toFloat()
		scoochworm.yBodyRot = scoochworm.yRot
	}

	private fun getInitialDirection(supportDirection: Direction): Vec3 {
		val minimumDirectionLengthSquared = 0.000001

		val rememberedDirection = scoochworm.rememberedMovementDirection
		if (rememberedDirection != null) {
			val projected = projectOntoSurface(rememberedDirection, supportDirection)
			if (projected.lengthSqr() > minimumDirectionLengthSquared) return projected.normalize()
		}

		val facing = Vec3.directionFromRotation(0f, scoochworm.yRot)
		val projected = projectOntoSurface(facing, supportDirection)
		if (projected.lengthSqr() > minimumDirectionLengthSquared) return projected.normalize()

		return if (supportDirection.axis == Direction.Axis.Y) {
			Vec3(0.0, 0.0, 1.0)
		} else {
			Vec3(0.0, 1.0, 0.0)
		}
	}

	private fun projectOntoSurface(direction: Vec3, supportDirection: Direction): Vec3 {
		val normal = supportDirection.normal.toVec3()
		return direction.subtract(normal.scale(direction.dot(normal)))
	}

	private fun getSupportAt(position: Vec3, supportDirection: Direction): ScoochwormSupport? {
		val supportPosition = ScoochwormEntity.getSupportBlockPosition(
			position,
			supportDirection
		)

		val candidate = ScoochwormSupport(supportPosition, supportDirection)

		return if (isAnySurface(candidate)) candidate else null
	}

	private fun getCrossedDirection(
		currentSupport: ScoochwormSupport,
		nextPosition: Vec3
	): Direction? {
		val nextSupport = ScoochwormEntity.getSupportBlockPosition(
			nextPosition,
			currentSupport.supportDirection
		)

		val difference = nextSupport.subtract(currentSupport.supportPosition)
		if (difference == BlockPos.ZERO) return null

		val direction = Direction.getNearest(
			difference.x.toDouble(),
			difference.y.toDouble(),
			difference.z.toDouble()
		)

		return if (direction.axis == currentSupport.supportDirection.axis) null else direction
	}

	private fun findCurrentSupport(): ScoochwormSupport? {
		for (supportDirection in Direction.entries) {
			val candidate = getSupportAt(scoochworm.position(), supportDirection) ?: continue
			return candidate
		}

		return null
	}

	private fun snapToSurface(newSupport: ScoochwormSupport) {
		val blockCenter = newSupport.supportPosition.center
		val normal = newSupport.supportDirection.normal.toVec3()
		val desiredCenter = blockCenter.subtract(
			normal.scale(0.5 + ScoochwormEntity.SIZE / 2.0)
		)
		val currentCenter = scoochworm.position().add(0.0, ScoochwormEntity.SIZE / 2.0, 0.0)
		// Only snap along the surface normal. Tangential coordinates should remain smooth.
		val snappedCenter = Vec3(
			if (normal.x == 0.0) currentCenter.x else desiredCenter.x,
			if (normal.y == 0.0) currentCenter.y else desiredCenter.y,
			if (normal.z == 0.0) currentCenter.z else desiredCenter.z
		)
		scoochworm.setPos(snappedCenter.subtract(0.0, ScoochwormEntity.SIZE / 2.0, 0.0))
	}

	private fun snapToEntryEdge(newSupport: ScoochwormSupport, movementDirection: Direction) {
		val blockCenter = newSupport.supportPosition.center
		val currentCenter = scoochworm.position().add(0.0, ScoochwormEntity.SIZE / 2.0, 0.0)

		val surfaceClearance = 0.001
		val entryOffset = 0.5 + ScoochwormEntity.SIZE / 2.0 + surfaceClearance

		val direction = movementDirection.normal
		val entryCenter = Vec3(
			if (direction.x == 0) currentCenter.x else blockCenter.x - direction.x * entryOffset,
			if (direction.y == 0) currentCenter.y else blockCenter.y - direction.y * entryOffset,
			if (direction.z == 0) currentCenter.z else blockCenter.z - direction.z * entryOffset
		)

		scoochworm.setPos(entryCenter.subtract(0.0, ScoochwormEntity.SIZE / 2.0, 0.0))
	}

	private fun isStem(surface: ScoochwormSupport): Boolean {
		return ScoochwormEntity.supportsScoochwormTravel(
			scoochworm.level(),
			surface.supportPosition,
			surface.supportDirection.opposite
		)
	}

	private fun isFreeSurface(surface: ScoochwormSupport): Boolean {
		return ScoochwormEntity.supportsFreeTravel(
			scoochworm.level(),
			surface.supportPosition,
			surface.supportDirection.opposite
		)
	}

	private fun isAnySurface(surface: ScoochwormSupport): Boolean {
		return isStem(surface) || isFreeSurface(surface)
	}

	private fun hasNoCollision(position: BlockPos): Boolean {
		val level = scoochworm.level()
		return level.getBlockState(position)
			.getCollisionShape(level, position)
			.isEmpty
	}

	private fun turnAlongSurface(
		direction: Direction,
		supportDirection: Direction,
		clockwise: Boolean
	): Direction {
		val first = if (clockwise) supportDirection.normal else direction.normal
		val second = if (clockwise) direction.normal else supportDirection.normal

		return Direction.fromDelta(
			first.y * second.z - first.z * second.y,
			first.z * second.x - first.x * second.z,
			first.x * second.y - first.y * second.x
		) ?: direction
	}

	private fun rotateAroundAxis(vector: Vec3, axis: Vec3, angle: Double): Vec3 {
		val cosine = Mth.cos(angle.toFloat()).toDouble()
		val sine = Mth.sin(angle.toFloat()).toDouble()
		// Rodrigues' formula lets the same wandering turn work on every surface orientation.
		return vector.scale(cosine)
			.add(axis.cross(vector).scale(sine))
			.add(axis.scale(axis.dot(vector) * (1.0 - cosine)))
	}

	companion object {
		private const val TURN_INTERVAL_TICKS = 20
		private const val TRANSITION_TICKS = 4
	}
}