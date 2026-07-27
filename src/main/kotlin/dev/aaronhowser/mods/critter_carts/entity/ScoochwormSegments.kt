package dev.aaronhowser.mods.critter_carts.entity

import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes

class ScoochwormSegments(
	private val scoochworm: ScoochwormEntity
) {

	private val bodyParts: MutableList<ScoochwormPartEntity> = mutableListOf()

	var count = 1
		private set

	val canGrow: Boolean
		get() = count < MAX_COUNT

	fun contains(partIndex: Int): Boolean {
		return partIndex in 0 until count
	}

	fun grow() {
		if (canGrow) {
			count++
		}
	}

	fun removeFrom(partIndex: Int) {
		if (!contains(partIndex)) return

		count = partIndex

		while (bodyParts.size > count) {
			bodyParts.removeLast().discard()
		}
	}

	fun update(path: ScoochwormPath) {
		ensureBodyParts(path)

		for (partIndex in bodyParts.indices) {
			val partPosition = path.getPosition(getPartDistance(partIndex))
			val part = bodyParts[partIndex]
			part.moveAlongPath(partPosition, scoochworm.xRot)
		}
	}

	fun restoreCount(segmentCount: Int) {
		count = segmentCount.coerceIn(0, MAX_COUNT)
	}

	fun discard() {
		for (bodyPart in bodyParts) {
			bodyPart.discard()
		}

		bodyParts.clear()
	}

	private fun ensureBodyParts(path: ScoochwormPath) {
		if (bodyParts.any(ScoochwormPartEntity::isRemoved)) {
			discard()
		}

		while (bodyParts.size < count) {
			createPart(path)
		}
	}

	private fun createPart(path: ScoochwormPath) {
		val partIndex = bodyParts.size
		val bodyPart = ScoochwormPartEntity(
			ModEntityTypes.SCOOCHWORM_PART.get(),
			scoochworm.level()
		)

		val partPosition = path.getPosition(getPartDistance(partIndex))

		bodyPart.attachTo(scoochworm, partIndex)
		bodyPart.moveTo(
			partPosition.x,
			partPosition.y,
			partPosition.z,
			scoochworm.yRot,
			scoochworm.xRot
		)

		scoochworm.level().addFreshEntity(bodyPart)
		bodyParts.add(bodyPart)
	}

	private fun getPartDistance(partIndex: Int): Double {
		return ScoochwormEntity.PART_SPACING * (partIndex + 1)
	}

	companion object {
		const val MAX_COUNT = 8
	}
}