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
		speed: Double
	) {
		travelDirection = newTravelDirection
		setWantedPosition(x, y, z, speed)
	}

	override fun tick() {
		val currentTravelDirection = travelDirection
		if (operation != Operation.MOVE_TO || currentTravelDirection == null) {
			scoochworm.zza = 0f
			return
		}

		scoochworm.yRot = currentTravelDirection.toYRot()
		scoochworm.yBodyRot = scoochworm.yRot
		scoochworm.speed = (speedModifier * scoochworm.getAttributeValue(Attributes.MOVEMENT_SPEED)).toFloat()

		val movementSpeed = scoochworm.speed.toDouble()
		scoochworm.deltaMovement = Vec3(
			currentTravelDirection.stepX * movementSpeed,
			currentTravelDirection.stepY * movementSpeed,
			currentTravelDirection.stepZ * movementSpeed
		)

		scoochworm.zza = 0f
		scoochworm.xxa = 0f

		operation = Operation.WAIT
	}

}