package dev.aaronhowser.mods.critter_carts.entity.data

import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import net.minecraft.nbt.ListTag

class ScoochwormSegments(
	private val scoochworm: ScoochwormEntity
) {

	private val segments: MutableList<ScoochwormSegment> = mutableListOf(ScoochwormSegment())

	val canGrow: Boolean
		get() = segments.size < MAX_COUNT

	fun contains(partIndex: Int): Boolean {
		return partIndex in segments.indices
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
			val segment = segments.removeLast()
			segment.dropAttachmentItem(scoochworm)
			segment.discardBodyPart()
		}
	}

	fun dropAllAttachmentItems() {
		for (segment in segments) {
			segment.dropAttachmentItem(scoochworm)
		}
	}

	fun getSegment(partIndex: Int): ScoochwormSegment? {
		return segments.getOrNull(partIndex)
	}

	fun update(path: ScoochwormPath) {
		for (partIndex in segments.indices) {
			val partPosition = path.getPosition(getPartDistance(partIndex))
			segments[partIndex].updateBodyPart(scoochworm, partIndex, partPosition)
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
		discard()
		segments.clear()

		val segmentCount = tag.size.coerceIn(MIN_COUNT, MAX_COUNT)
		for (index in 0 until segmentCount) {
			segments.add(
				if (index < tag.size) {
					ScoochwormSegment.load(tag.getCompound(index))
				} else {
					ScoochwormSegment()
				}
			)
		}
	}

	fun discard() {
		for (segment in segments) {
			segment.discardBodyPart()
		}
	}

	private fun getPartDistance(partIndex: Int): Double {
		return ScoochwormEntity.PART_SPACING * (partIndex + 1)
	}

	companion object {
		private const val MIN_COUNT = 1
		const val MAX_COUNT = 16
	}
}