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

	fun setWantedPosition(x: Double, y: Double, z: Double, direction: Direction, speed: Double) {
		if (travelDirection != null && travelDirection != direction) {
			rotateMomentum(direction)
		}

		travelDirection = direction
		setWantedPosition(x, y, z, speed)
	}

	override fun tick() {
		val direction = travelDirection
		if (operation != Operation.MOVE_TO || direction == null) {
			scoochworm.zza = 0f
			return
		}

		centerOnPath(direction)
		removeSidewaysMomentum(direction)

		scoochworm.yRot = direction.toYRot()
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
		val movement = scoochworm.deltaMovement
		scoochworm.deltaMovement = if (direction.axis == Direction.Axis.X) {
			movement.multiply(1.0, 1.0, 0.0)
		} else {
			movement.multiply(0.0, 1.0, 1.0)
		}
	}

	private fun rotateMomentum(direction: Direction) {
		val movement = scoochworm.deltaMovement
		val horizontalSpeed = sqrt(movement.x * movement.x + movement.z * movement.z)
		scoochworm.deltaMovement = Vec3(
			direction.stepX * horizontalSpeed,
			movement.y,
			direction.stepZ * horizontalSpeed
		)
	}
}