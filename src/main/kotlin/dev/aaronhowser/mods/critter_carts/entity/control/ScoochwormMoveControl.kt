package dev.aaronhowser.mods.critter_carts.entity.control

import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import net.minecraft.core.Direction
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.control.MoveControl
import net.minecraft.world.phys.Vec3
import kotlin.math.sqrt

class ScoochwormMoveControl(
	private val scoochworm: ScoochwormEntity
) : MoveControl(scoochworm) {

	private var travelDirection: Direction? = null

	fun setWantedPosition(x: Double, y: Double, z: Double, newTravelDirection: Direction, speed: Double) {
		if (travelDirection != null && travelDirection != newTravelDirection) {
			rotateMomentum(newTravelDirection)
		}

		travelDirection = newTravelDirection
		setWantedPosition(x, y, z, speed)
	}

	override fun tick() {
		val currentTravelDirection = travelDirection
		if (operation != Operation.MOVE_TO || currentTravelDirection == null) {
			scoochworm.zza = 0f
			return
		}

		centerOnPath(currentTravelDirection)
		removeSidewaysMomentum(currentTravelDirection)

		scoochworm.yRot = currentTravelDirection.toYRot()
		scoochworm.yBodyRot = scoochworm.yRot
		scoochworm.speed = (speedModifier * scoochworm.getAttributeValue(Attributes.MOVEMENT_SPEED)).toFloat()
		scoochworm.zza = 1f
		scoochworm.xxa = 0f
		operation = Operation.WAIT
	}

	private fun centerOnPath(direction: Direction) {
		if (direction.axis == Direction.Axis.X) {
			scoochworm.setPos(scoochworm.x, scoochworm.y, wantedZ)
		} else {
			scoochworm.setPos(wantedX, scoochworm.y, scoochworm.z)
		}
	}

	private fun removeSidewaysMomentum(direction: Direction) {
		val velocity = scoochworm.deltaMovement
		scoochworm.deltaMovement = if (direction.axis == Direction.Axis.X) {
			velocity.multiply(1.0, 1.0, 0.0)
		} else {
			velocity.multiply(0.0, 1.0, 1.0)
		}
	}

	private fun rotateMomentum(direction: Direction) {
		val velocity = scoochworm.deltaMovement
		val horizontalSpeed = sqrt(velocity.x * velocity.x + velocity.z * velocity.z)
		scoochworm.deltaMovement = Vec3(
			direction.stepX * horizontalSpeed,
			velocity.y,
			direction.stepZ * horizontalSpeed
		)
	}
}