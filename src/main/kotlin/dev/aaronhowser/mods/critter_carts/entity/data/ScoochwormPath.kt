package dev.aaronhowser.mods.critter_carts.entity.data

import net.minecraft.world.phys.Vec3
import java.util.*

class ScoochwormPath(
	private val initialLength: Double
) {

	private val positions = ArrayDeque<Vec3>()

	fun record(headPos: Vec3, yaw: Float) {
		if (positions.isEmpty()) {
			positions.addFirst(headPos)

			val forwardDirection = Vec3.directionFromRotation(0f, yaw)
			val firstSegmentPos = headPos.subtract(forwardDirection.scale(initialLength))
			positions.addLast(firstSegmentPos)
			return
		}

		if (positions.first.distanceToSqr(headPos) > MINIMUM_STEP_DISTANCE_SQUARED) {
			positions.addFirst(headPos)
		}

		while (positions.size > MAX_PATH_POINTS) {
			positions.removeLast()
		}
	}

	fun getPosition(distanceFromHead: Double): Vec3 {
		var remainingDistance = distanceFromHead
		val iterator = positions.iterator()
		var positionCloserToHead = iterator.next()

		while (iterator.hasNext()) {
			val positionFartherFromHead = iterator.next()
			val segmentLength = positionCloserToHead.distanceTo(positionFartherFromHead)

			if (segmentLength < remainingDistance || segmentLength <= 0.0) {
				remainingDistance -= segmentLength
				positionCloserToHead = positionFartherFromHead
				continue
			}

			return positionCloserToHead.lerp(
				positionFartherFromHead,
				remainingDistance / segmentLength
			)
		}

		return positionCloserToHead
	}

	fun clear() {
		positions.clear()
	}

	companion object {
		private const val MAX_PATH_POINTS = 256
		private const val MINIMUM_STEP_DISTANCE_SQUARED = 0.000001
	}
}