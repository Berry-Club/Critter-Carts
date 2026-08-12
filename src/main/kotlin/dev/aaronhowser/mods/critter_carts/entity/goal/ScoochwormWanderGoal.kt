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

	private var support: ScoochwormSupport? = null
	private var travelDirection = Vec3.ZERO
	private var nextTurnTick = 0
	private var transitionTicks = 0

	init {
		flags = EnumSet.of(Flag.MOVE)
	}

	override fun canUse(): Boolean {
		if (!scoochworm.isTryingToMove) return false

		val currentSupport = findCurrentSupport() ?: return false
		if (isStem(currentSupport)) return false

		support = currentSupport
		travelDirection = getInitialDirection(currentSupport.supportDirection)
		scoochworm.attachToSupport(
			currentSupport.supportPosition,
			currentSupport.supportDirection
		)

		return true
	}

	override fun canContinueToUse(): Boolean {
		val currentSupport = support ?: return false
		return scoochworm.isTryingToMove && !isStem(currentSupport)
	}

	override fun start() {
		scoochworm.isNoGravity = true
		scoochworm.noPhysics = false
		nextTurnTick = scoochworm.tickCount + TURN_INTERVAL_TICKS
	}

	override fun tick() {
		val currentSupport = support ?: return

		if (transitionTicks == 0 && scoochworm.tickCount >= nextTurnTick) {
			turn(currentSupport.supportDirection)
			nextTurnTick = scoochworm.tickCount + TURN_INTERVAL_TICKS
		}

		val movementSpeed = scoochworm.getAttributeValue(Attributes.MOVEMENT_SPEED)
		val movement = travelDirection.scale(movementSpeed)
		val nextPosition = scoochworm.position().add(movement)
		if (transitionTicks > 0) {
			continueTransition(currentSupport, nextPosition, movement)
			return
		}

		val wallDirection = getWallDirection(currentSupport, nextPosition)
		if (wallDirection != null) {
			val wallSupport = getWallSupport(currentSupport, wallDirection)
			if (wallSupport != null) {
				transitionTo(
					wallSupport,
					currentSupport.supportDirection.opposite,
					false
				)
				return
			}
		}

		val nextSupport = getSupportAt(nextPosition, currentSupport.supportDirection)

		if (nextSupport != null) {
			if (isStem(nextSupport)) {
				handOffToStem(nextSupport)
				return
			}

			support = nextSupport
			scoochworm.attachToSupport(nextSupport.supportPosition, nextSupport.supportDirection)
			move(movement)
			return
		}

		val crossedDirection = getCrossedDirection(currentSupport, nextPosition)
		if (crossedDirection == null) {
			move(movement)
			return
		}

		val edgeSupport = ScoochwormSupport(
			currentSupport.supportPosition,
			crossedDirection.opposite
		)
		if (isFreeSurface(edgeSupport)) {
			transitionTo(edgeSupport, currentSupport.supportDirection, true)
			return
		}

		travelDirection = travelDirection.scale(-1.0)
		move(Vec3.ZERO)
	}

	override fun stop() {
		scoochworm.noPhysics = false
		scoochworm.isTraversingSurfaceCorner = false
		transitionTicks = 0
		support = null
	}

	private fun turn(supportDirection: Direction) {
		val angle = scoochworm.random.nextDouble() * MAX_TURN_RADIANS
		val signedAngle = if (scoochworm.random.nextBoolean()) angle else -angle
		travelDirection = rotateAroundAxis(
			travelDirection,
			Vec3.atLowerCornerOf(supportDirection.normal),
			signedAngle
		).normalize()
	}

	private fun transitionTo(
		newSupport: ScoochwormSupport,
		newDirection: Direction,
		snapToEntry: Boolean
	) {
		support = newSupport
		travelDirection = curveDirectionOntoSurface(newSupport, newDirection)
		transitionTicks = TRANSITION_TICKS
		scoochworm.noPhysics = true
		scoochworm.isTraversingSurfaceCorner = true
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

		val directionAlongCorner = travelDirection.subtract(
			newSurfaceNormal.scale(travelDirection.dot(newSurfaceNormal))
		)

		val directionIntoCorner = abs(travelDirection.dot(newSurfaceNormal))

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
				scoochworm.isTraversingSurfaceCorner = false
				return
			}

			support = nextSupport
			scoochworm.attachToSupport(nextSupport.supportPosition, nextSupport.supportDirection)
		}

		move(movement)
		transitionTicks--
		if (transitionTicks == 0) {
			scoochworm.noPhysics = false
			scoochworm.isTraversingSurfaceCorner = false
		}
	}

	private fun handOffToStem(stemSupport: ScoochwormSupport) {
		support = stemSupport
		scoochworm.surfaceTravelDirection = travelDirection
		scoochworm.attachToSupport(stemSupport.supportPosition, stemSupport.supportDirection)
		snapToSurface(stemSupport)
		scoochworm.deltaMovement = Vec3.ZERO
	}

	private fun move(movement: Vec3) {
		scoochworm.deltaMovement = movement
		scoochworm.yRot = Mth.atan2(-travelDirection.x, travelDirection.z)
			.times(Mth.RAD_TO_DEG)
			.toFloat()
		scoochworm.yBodyRot = scoochworm.yRot
	}

	private fun getInitialDirection(supportDirection: Direction): Vec3 {
		val rememberedDirection = scoochworm.surfaceTravelDirection
		if (rememberedDirection != null) {
			val projected = projectOntoSurface(rememberedDirection, supportDirection)
			if (projected.lengthSqr() > MINIMUM_DIRECTION_LENGTH_SQUARED) return projected.normalize()
		}

		val facing = Vec3.directionFromRotation(0f, scoochworm.yRot)
		val projected = projectOntoSurface(facing, supportDirection)
		if (projected.lengthSqr() > MINIMUM_DIRECTION_LENGTH_SQUARED) return projected.normalize()

		return if (supportDirection.axis == Direction.Axis.Y) {
			Vec3(0.0, 0.0, 1.0)
		} else {
			Vec3(0.0, 1.0, 0.0)
		}
	}

	private fun projectOntoSurface(direction: Vec3, supportDirection: Direction): Vec3 {
		val normal = Vec3.atLowerCornerOf(supportDirection.normal)
		return direction.subtract(normal.scale(direction.dot(normal)))
	}

	private fun getSupportAt(position: Vec3, supportDirection: Direction): ScoochwormSupport? {
		val center = position.add(0.0, ScoochwormEntity.SIZE / 2.0, 0.0)
		val probe = center.add(
			Vec3.atLowerCornerOf(supportDirection.normal)
				.scale(ScoochwormEntity.SIZE / 2.0 + SUPPORT_PROBE_DISTANCE)
		)
		val candidate = ScoochwormSupport(BlockPos.containing(probe), supportDirection)

		return if (isAnySurface(candidate)) candidate else null
	}

	private fun getCrossedDirection(
		currentSupport: ScoochwormSupport,
		nextPosition: Vec3
	): Direction? {
		val nextSupport = getSupportBlockPosition(nextPosition, currentSupport.supportDirection)
		val difference = nextSupport.subtract(currentSupport.supportPosition)
		if (difference == BlockPos.ZERO) return null

		val direction = Direction.getNearest(
			difference.x.toDouble(),
			difference.y.toDouble(),
			difference.z.toDouble()
		)

		return if (direction.axis == currentSupport.supportDirection.axis) null else direction
	}

	private fun getWallDirection(
		currentSupport: ScoochwormSupport,
		nextPosition: Vec3
	): Direction? {
		val supportAxis = currentSupport.supportDirection.axis
		val probeDistance = ScoochwormEntity.SIZE / 2.0 + SUPPORT_PROBE_DISTANCE
		val leadingOffset = Vec3(
			if (supportAxis == Direction.Axis.X) 0.0 else Mth.sign(travelDirection.x) * probeDistance,
			if (supportAxis == Direction.Axis.Y) 0.0 else Mth.sign(travelDirection.y) * probeDistance,
			if (supportAxis == Direction.Axis.Z) 0.0 else Mth.sign(travelDirection.z) * probeDistance
		)
		val leadingPosition = nextPosition.add(leadingOffset)
		return getCrossedDirection(currentSupport, leadingPosition)
	}

	private fun getSupportBlockPosition(position: Vec3, supportDirection: Direction): BlockPos {
		val center = position.add(0.0, ScoochwormEntity.SIZE / 2.0, 0.0)
		val probe = center.add(
			Vec3.atLowerCornerOf(supportDirection.normal)
				.scale(ScoochwormEntity.SIZE / 2.0 + SUPPORT_PROBE_DISTANCE)
		)
		return BlockPos.containing(probe)
	}

	private fun getWallSupport(
		currentSupport: ScoochwormSupport,
		crossedDirection: Direction
	): ScoochwormSupport? {
		val wallPosition = currentSupport.supportPosition
			.relative(currentSupport.supportDirection.opposite)
			.relative(crossedDirection)
		val wallSupport = ScoochwormSupport(wallPosition, crossedDirection)
		return if (isAnySurface(wallSupport)) wallSupport else null
	}

	private fun findCurrentSupport(): ScoochwormSupport? {
		for (supportDirection in Direction.entries) {
			val candidate = getSupportAt(scoochworm.position(), supportDirection) ?: continue
			return candidate
		}

		return null
	}

	private fun snapToSurface(newSupport: ScoochwormSupport) {
		val blockCenter = Vec3.atCenterOf(newSupport.supportPosition)
		val normal = Vec3.atLowerCornerOf(newSupport.supportDirection.normal)
		val desiredCenter = blockCenter.subtract(
			normal.scale(0.5 + ScoochwormEntity.SIZE / 2.0)
		)
		val currentCenter = scoochworm.position().add(0.0, ScoochwormEntity.SIZE / 2.0, 0.0)
		val snappedCenter = Vec3(
			if (normal.x == 0.0) currentCenter.x else desiredCenter.x,
			if (normal.y == 0.0) currentCenter.y else desiredCenter.y,
			if (normal.z == 0.0) currentCenter.z else desiredCenter.z
		)
		scoochworm.setPos(snappedCenter.subtract(0.0, ScoochwormEntity.SIZE / 2.0, 0.0))
	}

	private fun snapToEntryEdge(newSupport: ScoochwormSupport, travelDirection: Direction) {
		val blockCenter = Vec3.atCenterOf(newSupport.supportPosition)
		val currentCenter = scoochworm.position().add(0.0, ScoochwormEntity.SIZE / 2.0, 0.0)
		val entryOffset = 0.5 + ScoochwormEntity.SIZE / 2.0 + SURFACE_CLEARANCE
		val direction = travelDirection.normal
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

	private fun rotateAroundAxis(vector: Vec3, axis: Vec3, angle: Double): Vec3 {
		val cosine = Mth.cos(angle.toFloat()).toDouble()
		val sine = Mth.sin(angle.toFloat()).toDouble()
		return vector.scale(cosine)
			.add(axis.cross(vector).scale(sine))
			.add(axis.scale(axis.dot(vector) * (1.0 - cosine)))
	}

	companion object {
		private const val TURN_INTERVAL_TICKS = 20
		private const val TRANSITION_TICKS = 4
		private const val MAX_TURN_RADIANS = Math.PI / 6.0
		private const val SUPPORT_PROBE_DISTANCE = 0.05
		private const val SURFACE_CLEARANCE = 0.001
		private const val MINIMUM_DIRECTION_LENGTH_SQUARED = 0.000001
	}
}