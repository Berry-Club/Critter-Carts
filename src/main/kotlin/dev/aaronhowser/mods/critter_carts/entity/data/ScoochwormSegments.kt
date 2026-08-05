package dev.aaronhowser.mods.critter_carts.entity.data

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isClientSide
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import net.minecraft.nbt.ListTag
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player

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

	fun interact(
		player: Player,
		hand: InteractionHand,
		partIndex: Int,
		currentAttachment: ScoochwormPartAttachment
	): InteractionResult {
		val heldStack = player.getItemInHand(hand)

		if (scoochworm.isClientSide) {
			return ScoochwormSegment.predictInteraction(
				player,
				heldStack,
				currentAttachment
			)
		}

		val segment = getSegment(partIndex) ?: return InteractionResult.PASS
		return segment.interact(
			player,
			hand,
			heldStack,
			onSheared = { removeFrom(partIndex) }
		)
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
			val pathPoint = path.getPoint(getPartDistance(partIndex))
			segments[partIndex].updateBodyPart(scoochworm, partIndex, pathPoint)
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