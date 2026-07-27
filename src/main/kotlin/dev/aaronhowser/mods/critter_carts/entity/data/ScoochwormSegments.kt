package dev.aaronhowser.mods.critter_carts.entity.data

import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes
import net.minecraft.nbt.ListTag
import net.minecraft.world.SimpleContainer
import net.minecraft.world.item.ItemStack

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
			val bodyPart = bodyParts.getOrNull(segments.lastIndex) ?: scoochworm
			val segment = segments.removeLast()
			segment.dropAttachmentItem(bodyPart)
		}

		while (bodyParts.size > count) {
			bodyParts.removeLast().discard()
		}
	}

	fun dropAllAttachmentItems() {
		for (partIndex in segments.indices) {
			val segment = segments[partIndex]
			val bodyPart = bodyParts.getOrNull(partIndex) ?: scoochworm
			segment.dropAttachmentItem(bodyPart)
		}
	}

	fun getAttachment(partIndex: Int): ScoochwormPartAttachment? {
		return segments.getOrNull(partIndex)?.attachment
	}

	fun setAttachmentItem(
		partIndex: Int,
		attachmentItem: ItemStack
	): Boolean {
		val segment = segments.getOrNull(partIndex) ?: return false
		segment.equipAttachmentItem(attachmentItem)
		bodyParts.getOrNull(partIndex)?.attachment = segment.attachment
		return true
	}

	fun removeAttachmentItem(partIndex: Int): ItemStack {
		val segment = segments.getOrNull(partIndex) ?: return ItemStack.EMPTY
		val attachmentItem = segment.removeAttachmentItem()
		bodyParts.getOrNull(partIndex)?.attachment = ScoochwormPartAttachment.NONE
		return attachmentItem
	}

	fun getContainer(partIndex: Int): SimpleContainer? {
		return segments.getOrNull(partIndex)?.container
	}

	fun getBodyPart(partIndex: Int): ScoochwormPartEntity? {
		return bodyParts.getOrNull(partIndex)
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