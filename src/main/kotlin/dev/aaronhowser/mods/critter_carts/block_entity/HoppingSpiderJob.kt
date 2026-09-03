package dev.aaronhowser.mods.critter_carts.block_entity

import net.minecraft.nbt.CompoundTag
import java.util.*

class HoppingSpiderJob(
	val homeNodeUuid: UUID,
	val sourceNodeUuid: UUID,
	val destinationNodeUuid: UUID,
	val sourceSlot: Int,
	var phase: Phase = Phase.TO_SOURCE
) {

	val currentNodeUuid: UUID
		get() {
			return when (phase) {
				Phase.TO_SOURCE -> homeNodeUuid
				Phase.TO_DESTINATION -> sourceNodeUuid
				Phase.RETURNING -> destinationNodeUuid
			}
		}

	val targetNodeUuid: UUID
		get() {
			return when (phase) {
				Phase.TO_SOURCE -> sourceNodeUuid
				Phase.TO_DESTINATION -> destinationNodeUuid
				Phase.RETURNING -> homeNodeUuid
			}
		}

	fun save(): CompoundTag {
		val tag = CompoundTag()
		tag.putUUID(HOME_NODE_UUID_TAG, homeNodeUuid)
		tag.putUUID(SOURCE_NODE_UUID_TAG, sourceNodeUuid)
		tag.putUUID(DESTINATION_NODE_UUID_TAG, destinationNodeUuid)
		tag.putInt(SOURCE_SLOT_TAG, sourceSlot)
		tag.putString(PHASE_TAG, phase.name)
		return tag
	}

	enum class Phase {
		TO_SOURCE,
		TO_DESTINATION,
		RETURNING
	}

	companion object {
		private const val HOME_NODE_UUID_TAG = "HomeNodeUuid"
		private const val SOURCE_NODE_UUID_TAG = "SourceNodeUuid"
		private const val DESTINATION_NODE_UUID_TAG = "DestinationNodeUuid"
		private const val SOURCE_SLOT_TAG = "SourceSlot"
		private const val PHASE_TAG = "Phase"

		fun load(tag: CompoundTag): HoppingSpiderJob? {
			if (!tag.hasUUID(HOME_NODE_UUID_TAG)) return null
			if (!tag.hasUUID(SOURCE_NODE_UUID_TAG)) return null
			if (!tag.hasUUID(DESTINATION_NODE_UUID_TAG)) return null

			val phase = Phase.entries.firstOrNull { phase ->
				phase.name == tag.getString(PHASE_TAG)
			} ?: Phase.TO_SOURCE

			return HoppingSpiderJob(
				tag.getUUID(HOME_NODE_UUID_TAG),
				tag.getUUID(SOURCE_NODE_UUID_TAG),
				tag.getUUID(DESTINATION_NODE_UUID_TAG),
				tag.getInt(SOURCE_SLOT_TAG),
				phase
			)
		}
	}
}