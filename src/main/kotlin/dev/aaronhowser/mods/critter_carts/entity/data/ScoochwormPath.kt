package dev.aaronhowser.mods.critter_carts.entity.data

import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.world.phys.Vec3
import java.util.*

class ScoochwormPath(
	private val initialLength: Double
) {

	private val points = ArrayDeque<ScoochwormPathPoint>()

	fun record(
		headPosition: Vec3,
		supportDirection: Direction,
		forwardDirection: Vec3
	) {
		if (points.isEmpty()) {
			points.addFirst(ScoochwormPathPoint(headPosition, supportDirection, forwardDirection))

			val tailPosition = headPosition.subtract(forwardDirection.scale(initialLength))
			points.addLast(ScoochwormPathPoint(tailPosition, supportDirection, forwardDirection))
			return
		}

		val movedFarEnough = points.first.position
			.distanceToSqr(headPosition) > MINIMUM_STEP_DISTANCE_SQUARED
		if (movedFarEnough) {
			points.addFirst(ScoochwormPathPoint(headPosition, supportDirection, forwardDirection))
		}

		while (points.size > MAX_PATH_POINTS) {
			points.removeLast()
		}
	}

	fun getPoint(distanceFromHead: Double): ScoochwormPathPoint {
		var remainingDistance = distanceFromHead
		val iterator = points.iterator()
		var positionCloserToHead = iterator.next()

		// Keep going to more and more distant points
		// until it finds one that's farther
		// then lerp it
		while (iterator.hasNext()) {
			val positionFartherFromHead = iterator.next()
			val segmentLength = positionCloserToHead.position.distanceTo(positionFartherFromHead.position)

			if (segmentLength < remainingDistance || segmentLength <= 0.0) {
				remainingDistance -= segmentLength
				positionCloserToHead = positionFartherFromHead
				continue
			}

			val interpolationProgress = remainingDistance / segmentLength

			val position = positionCloserToHead.position.lerp(
				positionFartherFromHead.position,
				interpolationProgress
			)

			val supportDirection = positionCloserToHead.supportDirection

			val forwardDirection = positionCloserToHead.forwardDirection.lerp(
				positionFartherFromHead.forwardDirection,
				interpolationProgress
			).normalize()

			return ScoochwormPathPoint(position, supportDirection, forwardDirection)
		}

		return positionCloserToHead
	}

	fun isEmpty(): Boolean = points.isEmpty()

	fun save(): ListTag {
		val tag = ListTag()

		for (pathPoint in points) {
			val pointTag = CompoundTag()

			pointTag.putDouble(X_TAG, pathPoint.position.x)
			pointTag.putDouble(Y_TAG, pathPoint.position.y)
			pointTag.putDouble(Z_TAG, pathPoint.position.z)

			pointTag.putInt(BOTTOM_TAG, pathPoint.supportDirection.get3DDataValue())

			pointTag.putDouble(FORWARD_X_TAG, pathPoint.forwardDirection.x)
			pointTag.putDouble(FORWARD_Y_TAG, pathPoint.forwardDirection.y)
			pointTag.putDouble(FORWARD_Z_TAG, pathPoint.forwardDirection.z)

			tag.add(pointTag)
		}

		return tag
	}

	fun load(tag: ListTag) {
		points.clear()

		for (index in tag.indices) {
			val pointTag = tag.getCompound(index)

			val position = Vec3(
				pointTag.getDouble(X_TAG),
				pointTag.getDouble(Y_TAG),
				pointTag.getDouble(Z_TAG)
			)

			val supportDirection = Direction.from3DDataValue(pointTag.getInt(BOTTOM_TAG))

			val forwardDirection = if (pointTag.contains(FORWARD_X_TAG)) {
				Vec3(
					pointTag.getDouble(FORWARD_X_TAG),
					pointTag.getDouble(FORWARD_Y_TAG),
					pointTag.getDouble(FORWARD_Z_TAG)
				)
			} else {
				Vec3(0.0, 0.0, 1.0)
			}

			points.addLast(ScoochwormPathPoint(position, supportDirection, forwardDirection))
		}
	}

	companion object {
		private const val MAX_PATH_POINTS = 256
		private const val MINIMUM_STEP_DISTANCE_SQUARED = 0.000001
		private const val X_TAG = "X"
		private const val Y_TAG = "Y"
		private const val Z_TAG = "Z"
		private const val BOTTOM_TAG = "Bottom"
		private const val FORWARD_X_TAG = "ForwardX"
		private const val FORWARD_Y_TAG = "ForwardY"
		private const val FORWARD_Z_TAG = "ForwardZ"
	}
}