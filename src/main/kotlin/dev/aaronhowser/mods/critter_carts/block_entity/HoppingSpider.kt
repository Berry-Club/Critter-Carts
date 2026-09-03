package dev.aaronhowser.mods.critter_carts.block_entity

import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import java.util.UUID

class HoppingSpider {

	var state: State = State.IDLE
	var homeNodeUuid: UUID? = null
	var sourceNodeUuid: UUID? = null
	var destinationNodeUuid: UUID? = null
	var currentNodeUuid: UUID? = null
	var targetNodeUuid: UUID? = null
	var routeProgress: Double = 0.0
	var carriedStack: ItemStack = ItemStack.EMPTY
	var position: Vec3? = null

	fun save(registries: HolderLookup.Provider): CompoundTag {
		val tag = CompoundTag()
		tag.putString(STATE_TAG, state.name)
		tag.putDouble(ROUTE_PROGRESS_TAG, routeProgress)

		putUuid(tag, HOME_NODE_UUID_TAG, homeNodeUuid)
		putUuid(tag, SOURCE_NODE_UUID_TAG, sourceNodeUuid)
		putUuid(tag, DESTINATION_NODE_UUID_TAG, destinationNodeUuid)
		putUuid(tag, CURRENT_NODE_UUID_TAG, currentNodeUuid)
		putUuid(tag, TARGET_NODE_UUID_TAG, targetNodeUuid)

		if (!carriedStack.isEmpty) {
			tag.put(CARRIED_STACK_TAG, carriedStack.save(registries))
		}

		val position = position
		if (position != null) {
			tag.putDouble(POSITION_X_TAG, position.x)
			tag.putDouble(POSITION_Y_TAG, position.y)
			tag.putDouble(POSITION_Z_TAG, position.z)
		}

		return tag
	}

	fun reset() {
		state = State.IDLE
		homeNodeUuid = null
		sourceNodeUuid = null
		destinationNodeUuid = null
		currentNodeUuid = null
		targetNodeUuid = null
		routeProgress = 0.0
		carriedStack = ItemStack.EMPTY
		position = null
	}

	enum class State {
		IDLE,
		TO_SOURCE,
		TO_DESTINATION,
		RETURNING
	}

	companion object {
		private const val STATE_TAG = "State"
		private const val HOME_NODE_UUID_TAG = "HomeNodeUuid"
		private const val SOURCE_NODE_UUID_TAG = "SourceNodeUuid"
		private const val DESTINATION_NODE_UUID_TAG = "DestinationNodeUuid"
		private const val CURRENT_NODE_UUID_TAG = "CurrentNodeUuid"
		private const val TARGET_NODE_UUID_TAG = "TargetNodeUuid"
		private const val ROUTE_PROGRESS_TAG = "RouteProgress"
		private const val CARRIED_STACK_TAG = "CarriedStack"
		private const val POSITION_X_TAG = "PositionX"
		private const val POSITION_Y_TAG = "PositionY"
		private const val POSITION_Z_TAG = "PositionZ"

		fun load(tag: CompoundTag, registries: HolderLookup.Provider): HoppingSpider {
			val spider = HoppingSpider()
			spider.state = State.entries.firstOrNull { state ->
				state.name == tag.getString(STATE_TAG)
			} ?: State.IDLE
			spider.routeProgress = tag.getDouble(ROUTE_PROGRESS_TAG)

			spider.homeNodeUuid = getUuid(tag, HOME_NODE_UUID_TAG)
			spider.sourceNodeUuid = getUuid(tag, SOURCE_NODE_UUID_TAG)
			spider.destinationNodeUuid = getUuid(tag, DESTINATION_NODE_UUID_TAG)
			spider.currentNodeUuid = getUuid(tag, CURRENT_NODE_UUID_TAG)
			spider.targetNodeUuid = getUuid(tag, TARGET_NODE_UUID_TAG)

			val carriedStackTag = tag.getCompound(CARRIED_STACK_TAG)
			spider.carriedStack = ItemStack.parseOptional(registries, carriedStackTag)

			if (tag.contains(POSITION_X_TAG)) {
				spider.position = Vec3(
					tag.getDouble(POSITION_X_TAG),
					tag.getDouble(POSITION_Y_TAG),
					tag.getDouble(POSITION_Z_TAG)
				)
			}
			return spider
		}

		private fun putUuid(tag: CompoundTag, name: String, uuid: UUID?) {
			if (uuid != null) {
				tag.putUUID(name, uuid)
			}
		}

		private fun getUuid(tag: CompoundTag, name: String): UUID? {
			if (!tag.hasUUID(name)) return null
			return tag.getUUID(name)
		}
	}
}