package dev.aaronhowser.mods.critter_carts.entity.goal

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isBlock
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.toVec3
import dev.aaronhowser.mods.critter_carts.datagen.tag.ModBlockTagsProvider
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.Mth
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.phys.AABB
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

		movementDirection = getInitialDirection(currentSupport.supportDirection)
		attachTo(currentSupport)

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

		if (hasAvoidedSupport(nextPosition, currentSupport.supportDirection)) {
			redirectFromCollision()
			return
		}

		if (tryTurningUpwards(currentSupport, nextPosition)) return

		val nextSupport = getSupportAt(nextPosition, currentSupport.supportDirection)
		if (nextSupport != null) return moveOntoSupport(nextSupport, movement)

		// Crossing into another block without finding support means we reached an edge and
		// may be able to turn downwards onto the next side.
		val edgeDirection = getCrossedDirection(currentSupport, nextPosition)
		if (edgeDirection == null) {
			if (!move(movement)) {
				redirectFromCollision()
			}
			return
		}

		val downwardTurnSupport = ScoochwormSupport(
			currentSupport.supportPosition,
			edgeDirection.opposite
		)

		if (isFreeSurface(downwardTurnSupport)) {
			if (!transitionTo(downwardTurnSupport, currentSupport.supportDirection, true)) {
				redirectFromCollision()
			}
		} else {
			redirectFromCollision()
		}
	}

	override fun stop() {
		scoochworm.noPhysics = false
		finishTransition()
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

	private fun tryTurningUpwards(
		currentSupport: ScoochwormSupport,
		nextPosition: Vec3
	): Boolean {
		val upwardTurnDirection = getUpwardTurnDirection(
			currentSupport,
			nextPosition
		) ?: return false
		val upwardTurnSupport = getUpwardTurnSupport(
			currentSupport,
			upwardTurnDirection
		) ?: return false

		return transitionTo(
			upwardTurnSupport,
			currentSupport.supportDirection.opposite,
			false
		)
	}

	private fun moveOntoSupport(nextSupport: ScoochwormSupport, movement: Vec3) {
		if (isStem(nextSupport)) {
			handOffToStem(nextSupport)
			return
		}

		if (!move(movement)) {
			redirectFromCollision()
			return
		}

		attachTo(nextSupport)
	}

	private fun transitionTo(
		newSupport: ScoochwormSupport,
		newDirection: Direction,
		snapToEntry: Boolean
	): Boolean {
		var destination = getPositionOnSurface(newSupport)
		if (snapToEntry) {
			destination = getPositionAtEntryEdge(destination, newSupport, newDirection)
		}

		if (!canOccupyAt(destination, newSupport.supportDirection)) return false

		movementDirection = curveDirectionOntoSurface(newSupport, newDirection)
		transitionTicks = TRANSITION_TICKS
		scoochworm.isTurningAroundCorner = true
		attachTo(newSupport)
		scoochworm.setPos(destination)
		move(Vec3.ZERO)

		return true
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
				finishTransition()
				return
			}

			attachTo(nextSupport)
		}

		if (!move(movement)) {
			finishTransition()
			redirectFromCollision()
			return
		}

		transitionTicks--
		if (transitionTicks == 0) {
			finishTransition()
		}
	}

	private fun finishTransition() {
		transitionTicks = 0
		scoochworm.isTurningAroundCorner = false
	}

	private fun attachTo(support: ScoochwormSupport) {
		currentSupport = support
		scoochworm.attachToSupport(support.supportPosition, support.supportDirection)
	}

	private fun handOffToStem(stemSupport: ScoochwormSupport) {
		scoochworm.rememberedMovementDirection = movementDirection
		attachTo(stemSupport)
		snapToSurface(stemSupport)
		scoochworm.deltaMovement = Vec3.ZERO
	}

	private fun move(movement: Vec3): Boolean {
		val canMove = canMove(movement)
		scoochworm.deltaMovement = if (canMove) movement else Vec3.ZERO
		scoochworm.yRot = Mth.atan2(-movementDirection.x, movementDirection.z)
			.times(Mth.RAD_TO_DEG)
			.toFloat()
		scoochworm.yBodyRot = scoochworm.yRot

		return canMove
	}

	private fun redirectFromCollision() {
		val support = currentSupport
		if (support == null) {
			move(Vec3.ZERO)
			return
		}

		val surfaceNormal = support.supportDirection.normal.toVec3()
		val movementSpeed = scoochworm.getAttributeValue(Attributes.MOVEMENT_SPEED)
		val firstTurnSign = if (scoochworm.random.nextBoolean()) 1.0 else -1.0

		var turnDegrees = COLLISION_TURN_STEP_DEGREES
		while (turnDegrees <= MAXIMUM_COLLISION_TURN_DEGREES) {
			val turnRadians = Math.toRadians(turnDegrees)
			val turnSigns = doubleArrayOf(firstTurnSign, -firstTurnSign)

			for (turnSign in turnSigns) {
				val direction = rotateAroundAxis(
					movementDirection,
					surfaceNormal,
					turnRadians * turnSign
				).normalize()

				if (!canMove(direction.scale(movementSpeed))) continue

				movementDirection = direction
				move(Vec3.ZERO)
				return
			}

			turnDegrees += COLLISION_TURN_STEP_DEGREES
		}

		move(Vec3.ZERO)
	}

	private fun canMove(movement: Vec3): Boolean {
		if (movement == Vec3.ZERO) return true

		val destination = scoochworm.position().add(movement)
		if (hasAvoidedSupport(destination, scoochworm.supportDirection)) return false

		val destinationBounds = scoochworm.boundingBox
			.move(movement)
			.deflate(COLLISION_TOLERANCE)

		return canOccupy(destinationBounds)
	}

	private fun canOccupyAt(position: Vec3, supportDirection: Direction): Boolean {
		if (hasAvoidedSupport(position, supportDirection)) return false

		return canOccupy(getBoundsAt(position))
	}

	private fun hasAvoidedSupport(
		position: Vec3,
		supportDirection: Direction
	): Boolean {
		val supportProbe = supportDirection.normal.toVec3()
			.scale(SUPPORT_PROBE_DISTANCE)
		val supportBounds = getBoundsAt(position)
			.expandTowards(supportProbe)

		return containsAvoidedBlock(supportBounds)
	}

	private fun canOccupy(bounds: AABB): Boolean {
		if (!scoochworm.level().noCollision(scoochworm, bounds)) return false
		return !containsAvoidedBlock(bounds)
	}

	private fun getBoundsAt(position: Vec3): AABB {
		val offset = position.subtract(scoochworm.position())
		return scoochworm.boundingBox
			.move(offset)
			.deflate(COLLISION_TOLERANCE)
	}

	private fun containsAvoidedBlock(bounds: AABB): Boolean {
		val minimumPosition = BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ)
		val maximumPosition = BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ)

		for (position in BlockPos.betweenClosed(minimumPosition, maximumPosition)) {
			val blockState = scoochworm.level().getBlockState(position)
			if (blockState.isBlock(ModBlockTagsProvider.PREVENTS_SCOOCHWORM_WANDERING)) {
				return true
			}
		}

		return false
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

	private fun getUpwardTurnDirection(
		currentSupport: ScoochwormSupport,
		nextPosition: Vec3
	): Direction? {
		val supportAxis = currentSupport.supportDirection.axis
		val probeDistance = ScoochwormEntity.SIZE / 2.0 + SUPPORT_PROBE_DISTANCE
		// Push the probe to the leading corner on every axis tangent to the current surface.
		val leadingOffset = Vec3(
			if (supportAxis == Direction.Axis.X) 0.0 else Mth.sign(movementDirection.x) * probeDistance,
			if (supportAxis == Direction.Axis.Y) 0.0 else Mth.sign(movementDirection.y) * probeDistance,
			if (supportAxis == Direction.Axis.Z) 0.0 else Mth.sign(movementDirection.z) * probeDistance
		)
		val leadingPosition = nextPosition.add(leadingOffset)
		return getCrossedDirection(currentSupport, leadingPosition)
	}

	private fun getUpwardTurnSupport(
		currentSupport: ScoochwormSupport,
		crossedDirection: Direction
	): ScoochwormSupport? {
		val upwardTurnPosition = currentSupport.supportPosition
			.relative(currentSupport.supportDirection.opposite)
			.relative(crossedDirection)
		val upwardTurnSupport = ScoochwormSupport(upwardTurnPosition, crossedDirection)
		return if (isAnySurface(upwardTurnSupport)) upwardTurnSupport else null
	}

	private fun findCurrentSupport(): ScoochwormSupport? {
		for (supportDirection in Direction.entries) {
			val candidate = getSupportAt(scoochworm.position(), supportDirection) ?: continue
			return candidate
		}

		return null
	}

	private fun snapToSurface(newSupport: ScoochwormSupport) {
		scoochworm.setPos(getPositionOnSurface(newSupport))
	}

	private fun getPositionOnSurface(newSupport: ScoochwormSupport): Vec3 {
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
		return snappedCenter.subtract(0.0, ScoochwormEntity.SIZE / 2.0, 0.0)
	}

	private fun getPositionAtEntryEdge(
		position: Vec3,
		newSupport: ScoochwormSupport,
		movementDirection: Direction
	): Vec3 {
		val blockCenter = newSupport.supportPosition.center
		val currentCenter = position.add(0.0, ScoochwormEntity.SIZE / 2.0, 0.0)

		val surfaceClearance = 0.001
		val entryOffset = 0.5 + ScoochwormEntity.SIZE / 2.0 + surfaceClearance

		val direction = movementDirection.normal
		val entryCenter = Vec3(
			if (direction.x == 0) currentCenter.x else blockCenter.x - direction.x * entryOffset,
			if (direction.y == 0) currentCenter.y else blockCenter.y - direction.y * entryOffset,
			if (direction.z == 0) currentCenter.z else blockCenter.z - direction.z * entryOffset
		)

		return entryCenter.subtract(0.0, ScoochwormEntity.SIZE / 2.0, 0.0)
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
		private const val SUPPORT_PROBE_DISTANCE = 0.05
		private const val COLLISION_TOLERANCE = 0.001
		private const val COLLISION_TURN_STEP_DEGREES = 5.0
		private const val MAXIMUM_COLLISION_TURN_DEGREES = 90.0
	}
}