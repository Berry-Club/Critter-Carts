package dev.aaronhowser.mods.critter_carts.block_entity

import dev.aaronhowser.mods.aaron.block_entity.SyncingBlockEntity
import dev.aaronhowser.mods.critter_carts.handler.web.WebNetwork
import dev.aaronhowser.mods.critter_carts.handler.web.WebSavedData
import dev.aaronhowser.mods.critter_carts.handler.web.node.BlockAnchor
import dev.aaronhowser.mods.critter_carts.registry.ModBlockEntityTypes
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.items.IItemHandler
import java.util.UUID

class HoppingSpiderNestBlockEntity(
	pos: BlockPos,
	state: BlockState
) : SyncingBlockEntity(ModBlockEntityTypes.HOPPING_SPIDER_NEST.get(), pos, state) {

	override val syncImmediately: Boolean = true

	val hoppingSpiders: MutableList<HoppingSpider> = MutableList(STARTING_SPIDER_COUNT) {
		HoppingSpider()
	}

	private fun tick(level: ServerLevel) {
		assignJobs(level)

		for (spider in hoppingSpiders) {
			spider.tick(level, blockPos.center, ::setChanged)
		}
	}

	private fun assignJobs(level: ServerLevel) {
		val reservedSources: MutableSet<Pair<UUID, Int>> = mutableSetOf()
		val reservedDestinations: MutableSet<UUID> = mutableSetOf()
		for (spider in hoppingSpiders) {
			val job = spider.job ?: continue
			reservedSources.add(job.sourceNodeUuid to job.sourceSlot)
			reservedDestinations.add(job.destinationNodeUuid)
		}

		for (spider in hoppingSpiders) {
			if (spider.job != null) continue

			val job = findJob(level, reservedSources, reservedDestinations) ?: return
			spider.job = job
			spider.position = WebSavedData.get(level).getNode(job.homeNodeUuid)?.position
			reservedSources.add(job.sourceNodeUuid to job.sourceSlot)
			reservedDestinations.add(job.destinationNodeUuid)
			setChanged()
		}
	}

	private fun findJob(
		level: ServerLevel,
		reservedSources: Set<Pair<UUID, Int>>,
		reservedDestinations: Set<UUID>
	): HoppingSpiderJob? {
		val savedData = WebSavedData.get(level)
		for (network in savedData.getNetworksAt(blockPos)) {
			val nestNodes = getAnchors(network, blockPos)
			val inventoryNodes = getInventoryAnchors(level, network)

			for (sourceNode in inventoryNodes) {
				val sourceHandler = getItemHandler(level, sourceNode) ?: continue
				for (sourceSlot in 0 until sourceHandler.slots) {
					if (sourceNode.uuid to sourceSlot in reservedSources) continue

					val stack = sourceHandler.extractItem(sourceSlot, MAX_TRANSFER_SIZE, true)
					if (stack.isEmpty) continue

					for (destinationNode in inventoryNodes) {
						if (destinationNode.blockPos == sourceNode.blockPos) continue
						if (destinationNode.uuid in reservedDestinations) continue
						if (!canFullyInsert(level, destinationNode, stack)) continue

						val homeNode = findHomeNode(network, nestNodes, sourceNode, destinationNode)
							?: continue
						return HoppingSpiderJob(
							homeNode.uuid,
							sourceNode.uuid,
							destinationNode.uuid,
							sourceSlot
						)
					}
				}
			}
		}

		return null
	}

	private fun findHomeNode(
		network: WebNetwork,
		nestNodes: List<BlockAnchor>,
		sourceNode: BlockAnchor,
		destinationNode: BlockAnchor
	): BlockAnchor? {
		if (network.findShortestPath(sourceNode, destinationNode) == null) return null

		for (nestNode in nestNodes) {
			if (network.findShortestPath(nestNode, sourceNode) == null) continue
			if (network.findShortestPath(destinationNode, nestNode) != null) return nestNode
		}

		return null
	}

	private fun getInventoryAnchors(level: ServerLevel, network: WebNetwork): List<BlockAnchor> {
		val anchors: MutableList<BlockAnchor> = mutableListOf()
		for (node in network.getNodes()) {
			if (node !is BlockAnchor || node.blockPos == blockPos) continue
			if (getItemHandler(level, node) == null) continue
			anchors.add(node)
		}

		return anchors
	}

	private fun getAnchors(network: WebNetwork, pos: BlockPos): List<BlockAnchor> {
		val anchors: MutableList<BlockAnchor> = mutableListOf()
		for (node in network.getNodes()) {
			if (node is BlockAnchor && node.blockPos == pos) {
				anchors.add(node)
			}
		}

		return anchors
	}

	private fun getItemHandler(level: ServerLevel, anchor: BlockAnchor): IItemHandler? {
		return level.getCapability(Capabilities.ItemHandler.BLOCK, anchor.blockPos, anchor.face)
	}

	private fun canFullyInsert(level: ServerLevel, anchor: BlockAnchor, stack: ItemStack): Boolean {
		val handler = getItemHandler(level, anchor) ?: return false
		var remainder = stack
		for (slot in 0 until handler.slots) {
			remainder = handler.insertItem(slot, remainder, true)
			if (remainder.isEmpty) return true
		}

		return false
	}

	override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
		super.saveAdditional(tag, registries)
		val spidersTag = ListTag()
		for (spider in hoppingSpiders) {
			spidersTag.add(spider.save(registries))
		}
		tag.put(SPIDERS_TAG, spidersTag)
	}

	override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
		super.loadAdditional(tag, registries)
		hoppingSpiders.clear()
		val spidersTag = tag.getList(SPIDERS_TAG, CompoundTag.TAG_COMPOUND.toInt())
		for (index in spidersTag.indices) {
			hoppingSpiders.add(HoppingSpider.load(spidersTag.getCompound(index), registries))
		}
	}

	companion object {
		private const val SPIDERS_TAG = "HoppingSpiders"
		private const val STARTING_SPIDER_COUNT = 4
		private const val MAX_TRANSFER_SIZE = 64

		fun serverTick(level: Level, pos: BlockPos, state: BlockState, blockEntity: HoppingSpiderNestBlockEntity) {
			if (level is ServerLevel) {
				blockEntity.tick(level)
			}
		}
	}
}