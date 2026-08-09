package dev.aaronhowser.mods.critter_carts.entity.control

import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import net.minecraft.core.Direction
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.control.MoveControl
import net.minecraft.world.phys.Vec3

class ScoochwormMoveControl(
	private val scoochworm: ScoochwormEntity
) : MoveControl(scoochworm) {

	private var travelDirection: Direction? = null

	fun setWantedPosition(
		x: Double,
		y: Double,
		z: Double,
		newTravelDirection: Direction,
		ignoreCollisions: Boolean,
		speed: Double
	) {
		travelDirection = newTravelDirection
		scoochworm.noPhysics = ignoreCollisions
		setWantedPosition(x, y, z, speed)
	}

	override fun tick() {
		val currentTravelDirection = travelDirection
		if (operation != Operation.MOVE_TO || currentTravelDirection == null) {
			scoochworm.zza = 0f
			return
		}

		scoochworm.yRot = ScoochwormEntity.getMovementYaw(
			currentTravelDirection,
			scoochworm.attachmentBottom
		)

		scoochworm.yBodyRot = scoochworm.yRot
		scoochworm.speed = (speedModifier * scoochworm.getAttributeValue(Attributes.MOVEMENT_SPEED)).toFloat()

		val directionVector = Vec3.atLowerCornerOf(currentTravelDirection.normal)
		val remainingDistance = scoochworm.position()
			.vectorTo(Vec3(wantedX, wantedY, wantedZ))
			.dot(directionVector)
			.coerceAtLeast(0.0)

		val movementSpeed = minOf(
			scoochworm.speed.toDouble(),
			remainingDistance
		)

		scoochworm.deltaMovement = Vec3(
			directionVector.x * movementSpeed,
			directionVector.y * movementSpeed,
			directionVector.z * movementSpeed
		)

		scoochworm.zza = 0f
		scoochworm.xxa = 0f

		operation = Operation.WAIT
	}

}