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

	fun contains(index: Int): Boolean {
		return index in 0 until count
	}

	fun grow() {
		if (canGrow) {
			count++
		}
	}

	fun removeFrom(index: Int) {
		if (!contains(index)) return

		count = index

		while (bodyParts.size > count) {
			bodyParts.removeLast().discard()
		}
	}

	fun update(path: ScoochwormPath) {
		ensureBodyParts(path)

		for (index in bodyParts.indices) {
			val pathPosition = path.getPosition(getPartDistance(index))
			bodyParts[index].moveAlongPath(pathPosition, scoochworm.xRot)
		}
	}

	fun restoreCount(count: Int) {
		this.count = count.coerceIn(0, MAX_COUNT)
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
	}

	private fun getPartDistance(partIndex: Int): Double {
		return ScoochwormEntity.PART_SPACING * (partIndex + 1)
	}

	companion object {
		const val MAX_COUNT = 4
	}
}