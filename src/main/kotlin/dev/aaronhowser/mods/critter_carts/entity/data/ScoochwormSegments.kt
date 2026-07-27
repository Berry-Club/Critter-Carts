package dev.aaronhowser.mods.critter_carts.entity.data

import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes
import net.minecraft.nbt.ListTag

class ScoochwormSegments(
	private val scoochworm: ScoochwormEntity
) {

	private val segments: MutableList<ScoochwormSegment> = mutableListOf(ScoochwormSegment())
	private val bodyParts: MutableList<ScoochwormPartEntity> = mutableListOf()

	val count: Int
		get() = segments.size

	val canGrow: Boolean
		get() = count < MAX_COUNT

	fun contains(partIndex: Int): Boolean {
		return partIndex in 0 until count
	}

	fun grow() {
		if (canGrow) {
			segments.add(ScoochwormSegment())
		}
	}

	fun removeFrom(partIndex: Int) {
		if (!contains(partIndex)) return

		val remainingCount = partIndex.coerceAtLeast(MIN_COUNT)

		while (segments.size > remainingCount) {
			segments.removeLast()
		}

		while (bodyParts.size > count) {
			bodyParts.removeLast().discard()
		}
	}

	fun getAttachment(partIndex: Int): ScoochwormPartAttachment? {
		return segments.getOrNull(partIndex)?.attachment
	}

	fun setAttachment(
		partIndex: Int,
		attachment: ScoochwormPartAttachment
	): Boolean {
		val segment = segments.getOrNull(partIndex) ?: return false
		segment.attachment = attachment
		bodyParts.getOrNull(partIndex)?.attachment = attachment
		return true
	}

	fun update(path: ScoochwormPath) {
		ensureBodyParts(path)

		for (partIndex in bodyParts.indices) {
			val partPosition = path.getPosition(getPartDistance(partIndex))
			val part = bodyParts[partIndex]
			part.moveAlongPath(partPosition, scoochworm.xRot)
		}
	}

	fun save(): ListTag {
		val tag = ListTag()

		for (segment in segments) {
			tag.add(segment.save())
		}

		return tag
	}

	fun load(tag: ListTag) {
		segments.clear()

		for (index in 0 until minOf(tag.size, MAX_COUNT)) {
			segments.add(
				ScoochwormSegment.load(tag.getCompound(index))
			)
		}

		if (segments.isEmpty()) {
			segments.add(ScoochwormSegment())
		}
	}

	fun restoreLegacyCount(segmentCount: Int) {
		segments.clear()

		for (i in 0 until segmentCount.coerceIn(MIN_COUNT, MAX_COUNT)) {
			segments.add(ScoochwormSegment())
		}
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
		val segment = segments[partIndex]

		val partPosition = path.getPosition(getPartDistance(partIndex))

		bodyPart.attachTo(
			scoochworm,
			partIndex,
			segment.attachment
		)
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
		private const val MIN_COUNT = 1
		const val MAX_COUNT = 16
	}
}