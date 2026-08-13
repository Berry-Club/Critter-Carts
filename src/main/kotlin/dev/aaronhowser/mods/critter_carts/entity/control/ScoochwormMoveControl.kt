package dev.aaronhowser.mods.critter_carts.entity.control

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.toVec3
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import net.minecraft.core.Direction
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.control.MoveControl
import net.minecraft.world.phys.Vec3

class ScoochwormMoveControl(
	private val scoochworm: ScoochwormEntity
) : MoveControl(scoochworm) {

	private var movementDirection: Direction? = null

	fun setWantedPosition(
		x: Double,
		y: Double,
		z: Double,
		newMovementDirection: Direction,
		ignoreCollisions: Boolean,
		speed: Double
	) {
		movementDirection = newMovementDirection
		scoochworm.noPhysics = ignoreCollisions
		setWantedPosition(x, y, z, speed)
	}

	override fun tick() {
		val currentDirection = movementDirection
		if (operation != Operation.MOVE_TO || currentDirection == null) {
			scoochworm.zza = 0f
			return
		}

		val movementYaw = ScoochwormEntity.getMovementYaw(
			currentDirection,
			scoochworm.supportDirection
		)

		scoochworm.yRot = movementYaw
		scoochworm.yBodyRot = scoochworm.yRot

		val movementSpeedAttribute = scoochworm.getAttributeValue(Attributes.MOVEMENT_SPEED)
		val movementSpeed = speedModifier * movementSpeedAttribute
		scoochworm.speed = movementSpeed.toFloat()

		val directionVector = currentDirection.normal.toVec3()
		scoochworm.forwardDirection = directionVector
		val remainingDistance = scoochworm.position()
			.vectorTo(Vec3(wantedX, wantedY, wantedZ))
			.dot(directionVector)
			.coerceAtLeast(0.0)

		val limitedMovementSpeed = minOf(
			movementSpeed,
			remainingDistance
		)

		scoochworm.deltaMovement = directionVector.scale(limitedMovementSpeed)

		scoochworm.zza = 0f
		scoochworm.xxa = 0f

		operation = Operation.WAIT
	}
}