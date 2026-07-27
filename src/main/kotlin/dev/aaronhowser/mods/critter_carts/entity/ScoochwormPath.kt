package dev.aaronhowser.mods.critter_carts.entity

import net.minecraft.world.phys.Vec3
import java.util.ArrayDeque

class ScoochwormPath(
	private val initialLength: Double
) {

	private val positions = ArrayDeque<Vec3>()

	fun record(position: Vec3, yaw: Float) {
		if (positions.isEmpty()) {
			val forward = Vec3.directionFromRotation(0f, yaw)

			positions.addFirst(position)
			positions.addLast(
				position.subtract(forward.scale(initialLength))
			)
			return
		}

		if (positions.first.distanceToSqr(position) > MINIMUM_STEP_SQUARED) {
			positions.addFirst(position)
		}

		while (positions.size > MAX_POINTS) {
			positions.removeLast()
		}
	}

	fun getPosition(distance: Double): Vec3 {
		var remainingDistance = distance
		val iterator = positions.iterator()
		var newerPosition = iterator.next()

		while (iterator.hasNext()) {
			val olderPosition = iterator.next()
			val sectionDistance = newerPosition.distanceTo(olderPosition)

			if (sectionDistance >= remainingDistance && sectionDistance > 0.0) {
				return newerPosition.lerp(
					olderPosition,
					remainingDistance / sectionDistance
				)
			}

			remainingDistance -= sectionDistance
			newerPosition = olderPosition
		}

		return newerPosition
	}

	fun clear() {
		positions.clear()
	}

	companion object {
		private const val MAX_POINTS = 256
		private const val MINIMUM_STEP_SQUARED = 0.000001
	}
}