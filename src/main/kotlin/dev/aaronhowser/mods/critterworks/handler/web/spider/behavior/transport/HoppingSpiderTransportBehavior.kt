package dev.aaronhowser.mods.critterworks.handler.web.spider.behavior.transport

import dev.aaronhowser.mods.critterworks.handler.web.spider.HoppingSpider
import dev.aaronhowser.mods.critterworks.handler.web.spider.behavior.HoppingSpiderBehavior
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3
import java.util.*

class HoppingSpiderTransportBehavior(
	val homeNodeUuid: UUID,
	val sourceNodeUuid: UUID,
	val destinationNodeUuid: UUID,
	val sourceSlot: Int,
	val transferAmount: Int,
	val startingNodeUuid: UUID = homeNodeUuid,
	var phase: Phase = Phase.TO_SOURCE,
	var failureReason: FailureReason? = null
) : HoppingSpiderBehavior {

	override val priority: Int
		get() {
			if (phase == Phase.RETURNING || phase == Phase.RETURNING_FROM_SOURCE) return 50
			return 100
		}
	override val canBeInterrupted: Boolean
		get() = phase == Phase.RETURNING || phase == Phase.RETURNING_FROM_SOURCE

	override val currentNodeUuid: UUID
		get() {
			return when (phase) {
				Phase.TO_SOURCE -> startingNodeUuid
				Phase.TO_DESTINATION -> sourceNodeUuid
				Phase.RETURNING -> destinationNodeUuid
				Phase.RETURNING_ITEM -> destinationNodeUuid
				Phase.RETURNING_FROM_SOURCE -> sourceNodeUuid
			}
		}

	val targetNodeUuid: UUID
		get() {
			return when (phase) {
				Phase.TO_SOURCE -> sourceNodeUuid
				Phase.TO_DESTINATION -> destinationNodeUuid
				Phase.RETURNING -> homeNodeUuid
				Phase.RETURNING_ITEM -> sourceNodeUuid
				Phase.RETURNING_FROM_SOURCE -> homeNodeUuid
			}
		}

	override fun save(): CompoundTag {
		val tag = CompoundTag()
		tag.putString(TYPE_TAG, TYPE)

		tag.putUUID(HOME_NODE_UUID_TAG, homeNodeUuid)
		tag.putUUID(SOURCE_NODE_UUID_TAG, sourceNodeUuid)
		tag.putUUID(DESTINATION_NODE_UUID_TAG, destinationNodeUuid)
		tag.putInt(SOURCE_SLOT_TAG, sourceSlot)
		tag.putInt(TRANSFER_AMOUNT_TAG, transferAmount)
		tag.putUUID(STARTING_NODE_UUID_TAG, startingNodeUuid)
		tag.putString(PHASE_TAG, phase.name)

		val failureReason = failureReason
		if (failureReason != null) {
			tag.putString(FAILURE_REASON_TAG, failureReason.name)
		}

		return tag
	}

	override fun tick(level: ServerLevel, spider: HoppingSpider, nestPosition: Vec3): Boolean {
		return when (spider.travelTo(level, currentNodeUuid, targetNodeUuid)) {
			HoppingSpider.TravelResult.STARTED -> true
			HoppingSpider.TravelResult.TRAVELLING -> false
			HoppingSpider.TravelResult.ARRIVED -> spider.completeTransportLeg(level, this, nestPosition)
			HoppingSpider.TravelResult.MISSING_PATH -> spider.handleMissingTransportPath(level)
		}
	}

	enum class Phase {
		TO_SOURCE,
		TO_DESTINATION,
		RETURNING,
		RETURNING_ITEM,
		RETURNING_FROM_SOURCE
	}

	enum class FailureReason(
		val shouldRetry: Boolean
	) {
		DESTINATION_MISSING(false),
		SOURCE_MISSING(false),
		DESTINATION_NOT_OUTPUT(false),
		CHANNEL_CHANGED(false),
		FILTER_CHANGED(false),
		DESTINATION_UNAVAILABLE(false),
		DESTINATION_FULL(true)
	}

	companion object {
		const val TYPE = "transport"
		private const val TYPE_TAG = "Type"
		private const val HOME_NODE_UUID_TAG = "HomeNodeUuid"
		private const val SOURCE_NODE_UUID_TAG = "SourceNodeUuid"
		private const val DESTINATION_NODE_UUID_TAG = "DestinationNodeUuid"
		private const val SOURCE_SLOT_TAG = "SourceSlot"
		private const val TRANSFER_AMOUNT_TAG = "TransferAmount"
		private const val STARTING_NODE_UUID_TAG = "StartingNodeUuid"
		private const val PHASE_TAG = "Phase"
		private const val FAILURE_REASON_TAG = "FailureReason"

		fun load(tag: CompoundTag): HoppingSpiderTransportBehavior? {
			if (!tag.hasUUID(HOME_NODE_UUID_TAG)) return null
			if (!tag.hasUUID(SOURCE_NODE_UUID_TAG)) return null
			if (!tag.hasUUID(DESTINATION_NODE_UUID_TAG)) return null

			val phase = Phase.entries.firstOrNull { phase ->
				phase.name == tag.getString(PHASE_TAG)
			} ?: Phase.TO_SOURCE

			return HoppingSpiderTransportBehavior(
				tag.getUUID(HOME_NODE_UUID_TAG),
				tag.getUUID(SOURCE_NODE_UUID_TAG),
				tag.getUUID(DESTINATION_NODE_UUID_TAG),
				tag.getInt(SOURCE_SLOT_TAG),
				getTransferAmount(tag),
				getStartingNodeUuid(tag),
				phase,
				getFailureReason(tag)
			)
		}

		private fun getFailureReason(tag: CompoundTag): FailureReason? {
			if (!tag.contains(FAILURE_REASON_TAG)) return null

			return FailureReason.entries.firstOrNull { reason ->
				reason.name == tag.getString(FAILURE_REASON_TAG)
			}
		}

		private fun getTransferAmount(tag: CompoundTag): Int {
			if (!tag.contains(TRANSFER_AMOUNT_TAG)) return 64

			return tag.getInt(TRANSFER_AMOUNT_TAG)
		}

		private fun getStartingNodeUuid(tag: CompoundTag): UUID {
			if (tag.hasUUID(STARTING_NODE_UUID_TAG)) return tag.getUUID(STARTING_NODE_UUID_TAG)
			return tag.getUUID(HOME_NODE_UUID_TAG)
		}
	}
}
