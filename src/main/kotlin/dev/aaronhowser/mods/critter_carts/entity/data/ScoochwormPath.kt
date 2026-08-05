package dev.aaronhowser.mods.critter_carts.entity.data

import net.minecraft.world.phys.Vec3
import net.minecraft.core.Direction
import java.util.*

class ScoochwormPath(
	private val initialLength: Double
) {

	private val positions = ArrayDeque<ScoochwormPathPoint>()

	fun record(headPos: Vec3, bottom: Direction, yaw: Float) {
		if (positions.isEmpty()) {
			positions.addFirst(ScoochwormPathPoint(headPos, bottom))

			val forwardDirection = Vec3.directionFromRotation(0f, yaw)
			val firstSegmentPos = headPos.subtract(forwardDirection.scale(initialLength))
			positions.addLast(ScoochwormPathPoint(firstSegmentPos, bottom))
			return
		}

		val movedFarEnough = positions.first.position.distanceToSqr(headPos) > MINIMUM_STEP_DISTANCE_SQUARED
		if (movedFarEnough) {
			positions.addFirst(ScoochwormPathPoint(headPos, bottom))
		}

		while (positions.size > MAX_PATH_POINTS) {
			positions.removeLast()
		}
	}

	fun getPoint(distanceFromHead: Double): ScoochwormPathPoint {
		var remainingDistance = distanceFromHead
		val iterator = positions.iterator()
		var positionCloserToHead = iterator.next()

		while (iterator.hasNext()) {
			val positionFartherFromHead = iterator.next()
			val segmentLength = positionCloserToHead.position.distanceTo(positionFartherFromHead.position)

			if (segmentLength < remainingDistance || segmentLength <= 0.0) {
				remainingDistance -= segmentLength
				positionCloserToHead = positionFartherFromHead
				continue
			}

			return ScoochwormPathPoint(
				positionCloserToHead.position.lerp(
					positionFartherFromHead.position,
					remainingDistance / segmentLength
				),
				positionCloserToHead.bottom
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