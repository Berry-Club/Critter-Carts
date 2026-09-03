package dev.aaronhowser.mods.critter_carts.block_entity

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack

class HoppingSpider {

	var state: State = State.IDLE
	var sourcePos: BlockPos? = null
	var destinationPos: BlockPos? = null
	var routeProgress: Double = 0.0
	var carriedStack: ItemStack = ItemStack.EMPTY

	fun save(registries: HolderLookup.Provider): CompoundTag {
		val tag = CompoundTag()
		tag.putString(STATE_TAG, state.name)
		tag.putDouble(ROUTE_PROGRESS_TAG, routeProgress)

		val sourcePos = sourcePos
		if (sourcePos != null) {
			tag.putLong(SOURCE_POS_TAG, sourcePos.asLong())
		}

		val destinationPos = destinationPos
		if (destinationPos != null) {
			tag.putLong(DESTINATION_POS_TAG, destinationPos.asLong())
		}

		if (!carriedStack.isEmpty) {
			tag.put(CARRIED_STACK_TAG, carriedStack.save(registries))
		}

		return tag
	}

	fun reset() {
		state = State.IDLE
		sourcePos = null
		destinationPos = null
		routeProgress = 0.0
		carriedStack = ItemStack.EMPTY
	}

	enum class State {
		IDLE,
		TO_SOURCE,
		TO_DESTINATION,
		RETURNING
	}

	companion object {
		private const val STATE_TAG = "State"
		private const val SOURCE_POS_TAG = "SourcePos"
		private const val DESTINATION_POS_TAG = "DestinationPos"
		private const val ROUTE_PROGRESS_TAG = "RouteProgress"
		private const val CARRIED_STACK_TAG = "CarriedStack"

		fun load(tag: CompoundTag, registries: HolderLookup.Provider): HoppingSpider {
			val spider = HoppingSpider()
			spider.state = State.entries.firstOrNull { state ->
				state.name == tag.getString(STATE_TAG)
			} ?: State.IDLE
			spider.routeProgress = tag.getDouble(ROUTE_PROGRESS_TAG)

			if (tag.contains(SOURCE_POS_TAG)) {
				spider.sourcePos = BlockPos.of(tag.getLong(SOURCE_POS_TAG))
			}

			if (tag.contains(DESTINATION_POS_TAG)) {
				spider.destinationPos = BlockPos.of(tag.getLong(DESTINATION_POS_TAG))
			}

			val carriedStackTag = tag.getCompound(CARRIED_STACK_TAG)
			spider.carriedStack = ItemStack.parseOptional(registries, carriedStackTag)
			return spider
		}
	}
}