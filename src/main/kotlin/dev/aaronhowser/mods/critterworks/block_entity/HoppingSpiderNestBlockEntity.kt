package dev.aaronhowser.mods.critterworks.block_entity

import dev.aaronhowser.mods.aaron.block_entity.SyncingBlockEntity
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isTrue
import dev.aaronhowser.mods.critterworks.handler.web.WebNetwork
import dev.aaronhowser.mods.critterworks.handler.web.WebSavedData
import dev.aaronhowser.mods.critterworks.handler.web.node.WebBlockAnchor
import dev.aaronhowser.mods.critterworks.handler.web.spider.HoppingSpider
import dev.aaronhowser.mods.critterworks.handler.web.spider.HoppingSpiderJob
import dev.aaronhowser.mods.critterworks.handler.web.spider.HoppingSpiderJobCandidate
import dev.aaronhowser.mods.critterworks.handler.web.spider.HoppingSpiderReservations
import dev.aaronhowser.mods.critterworks.item.ItemFilterItem
import dev.aaronhowser.mods.critterworks.item.SpiderNestInterfaceItem
import dev.aaronhowser.mods.critterworks.item.component.NestInterfaceComponent
import dev.aaronhowser.mods.critterworks.menu.spider_nest.SpiderNestMenu
import dev.aaronhowser.mods.critterworks.registry.ModBlockEntityTypes
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.items.IItemHandler

class HoppingSpiderNestBlockEntity(
	pos: BlockPos,
	state: BlockState
) : SyncingBlockEntity(ModBlockEntityTypes.HOPPING_SPIDER_NEST.get(), pos, state), MenuProvider {

	override val syncImmediately: Boolean = false

	val hoppingSpiders: MutableList<HoppingSpider> = MutableList(STARTING_SPIDER_COUNT) {
		HoppingSpider()
	}

	override fun getDisplayName(): Component {
		return Component.translatable("menu.critterworks.spider_nest.title")
	}

	override fun createMenu(
		containerId: Int,
		playerInventory: Inventory,
		player: Player
	): AbstractContainerMenu {
		return SpiderNestMenu(containerId, playerInventory, this)
	}

	private fun serverTick(level: ServerLevel) {
		var shouldSync = assignJobs(level)

		for (spider in hoppingSpiders) {
			if (spider.serverTick(level, blockPos.center)) {
				shouldSync = true
			}
		}

		if (shouldSync || hasActiveSpiders()) {
			setChanged()
		}

		if (shouldSync) {
			level.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_CLIENTS)
		}
	}

	private fun hasActiveSpiders(): Boolean {
		for (spider in hoppingSpiders) {
			if (spider.job != null) return true
		}

		return false
	}

	private fun assignJobs(level: ServerLevel): Boolean {
		val reservations = getReservations(level)
		var assignedJob = false

		for (spider in hoppingSpiders) {
			if (spider.job != null) continue

			val job = findJob(level, reservations) ?: break

			assignJob(level, spider, job)
			reservations.reserve(job)

			assignedJob = true
		}

		return assignedJob
	}

	private fun getReservations(level: ServerLevel): HoppingSpiderReservations {
		val reservations = HoppingSpiderReservations()
		val nestPositions = mutableSetOf(blockPos)
		val savedData = WebSavedData.get(level)

		for (network in savedData.getNetworksAt(blockPos)) {
			for (node in network.getNodes()) {
				if (node !is WebBlockAnchor) continue
				if (level.getBlockEntity(node.blockPos) !is HoppingSpiderNestBlockEntity) continue

				nestPositions.add(node.blockPos)
			}
		}

		for (pos in nestPositions) {
			val nest = level.getBlockEntity(pos) as? HoppingSpiderNestBlockEntity ?: continue

			for (spider in nest.hoppingSpiders) {
				val job = spider.job ?: continue

				reservations.reserve(job)
			}
		}

		return reservations
	}

	private fun assignJob(level: ServerLevel, spider: HoppingSpider, job: HoppingSpiderJob) {
		spider.job = job

		val homeNode = WebSavedData.get(level).getNode(job.homeNodeUuid)
		spider.position = homeNode?.position
	}

	private fun findJob(level: ServerLevel, reservations: HoppingSpiderReservations): HoppingSpiderJob? {
		val savedData = WebSavedData.get(level)
		var bestCandidate: HoppingSpiderJobCandidate? = null

		for (network in savedData.getNetworksAt(blockPos)) {
			val candidate = findJobInNetwork(level, network, reservations)

			if (candidate?.isPreferredOver(bestCandidate) == true) {
				bestCandidate = candidate
			}
		}

		return bestCandidate?.job
	}

	private fun findJobInNetwork(
		level: ServerLevel,
		network: WebNetwork,
		reservations: HoppingSpiderReservations
	): HoppingSpiderJobCandidate? {
		val nestNodes = getAnchors(network, blockPos)
		val inventoryNodes = getInventoryAnchors(level, network)
		var bestCandidate: HoppingSpiderJobCandidate? = null

		for (sourceNode in inventoryNodes) {
			val candidate = findJobFromSource(
				level,
				network,
				nestNodes,
				inventoryNodes,
				sourceNode,
				reservations
			)

			if (candidate?.isPreferredOver(bestCandidate).isTrue()) {
				bestCandidate = candidate
			}
		}

		return bestCandidate
	}

	private fun findJobFromSource(
		level: ServerLevel,
		network: WebNetwork,
		nestNodes: List<WebBlockAnchor>,
		inventoryNodes: List<WebBlockAnchor>,
		sourceNode: WebBlockAnchor,
		reservations: HoppingSpiderReservations
	): HoppingSpiderJobCandidate? {
		val sourceInterface = SpiderNestInterfaceItem.getComponent(sourceNode.nestInterface)
		if (sourceInterface.transferDirection != NestInterfaceComponent.TransferDirection.INPUT) return null

		val sourceHandler = getItemHandler(level, sourceNode) ?: return null
		var bestCandidate: HoppingSpiderJobCandidate? = null

		for (sourceSlot in 0 until sourceHandler.slots) {
			if (reservations.isSourceReserved(sourceNode.uuid, sourceSlot)) continue

			val stack = sourceHandler.extractItem(sourceSlot, MAX_TRANSFER_SIZE, true)

			if (stack.isEmpty) continue
			if (!passesFilter(sourceInterface, stack)) continue

			val candidate = findDestinationJob(
				level,
				network,
				nestNodes,
				inventoryNodes,
				sourceNode,
				sourceSlot,
				stack,
				reservations
			)

			if (candidate?.isPreferredOver(bestCandidate) == true) {
				bestCandidate = candidate
			}
		}

		return bestCandidate
	}

	private fun findDestinationJob(
		level: ServerLevel,
		network: WebNetwork,
		nestNodes: List<WebBlockAnchor>,
		inventoryNodes: List<WebBlockAnchor>,
		sourceNode: WebBlockAnchor,
		sourceSlot: Int,
		stack: ItemStack,
		reservations: HoppingSpiderReservations
	): HoppingSpiderJobCandidate? {
		val sourceInterface = SpiderNestInterfaceItem.getComponent(sourceNode.nestInterface)
		var bestCandidate: HoppingSpiderJobCandidate? = null

		for (destinationNode in inventoryNodes) {
			if (isSameFace(sourceNode, destinationNode)) continue

			val destinationInterface = SpiderNestInterfaceItem.getComponent(destinationNode.nestInterface)

			if (destinationInterface.transferDirection != NestInterfaceComponent.TransferDirection.OUTPUT) continue
			if (destinationInterface.color != sourceInterface.color) continue
			if (!passesFilter(destinationInterface, stack)) continue
			if (reservations.isDestinationReserved(destinationNode.uuid)) continue

			val transferAmount = getInsertableAmount(level, destinationNode, stack)
			if (transferAmount == 0) continue

			val homeNode = findHomeNode(network, nestNodes, sourceNode, destinationNode)
				?: continue

			val job = HoppingSpiderJob(
				homeNode.uuid,
				sourceNode.uuid,
				destinationNode.uuid,
				sourceSlot,
				transferAmount
			)

			val candidate = HoppingSpiderJobCandidate(
				job,
				sourceInterface.priority,
				destinationInterface.priority,
				transferAmount
			)

			if (candidate.isPreferredOver(bestCandidate)) {
				bestCandidate = candidate
			}
		}

		return bestCandidate
	}

	private fun passesFilter(interfaceComponent: NestInterfaceComponent, stack: ItemStack): Boolean {
		val filter = interfaceComponent.getFilter()
		return filter.isEmpty || ItemFilterItem.passesFilter(filter, stack)
	}

	private fun isSameFace(first: WebBlockAnchor, second: WebBlockAnchor): Boolean {
		return first.blockPos == second.blockPos && first.face == second.face
	}

	private fun findHomeNode(
		network: WebNetwork,
		nestNodes: List<WebBlockAnchor>,
		sourceNode: WebBlockAnchor,
		destinationNode: WebBlockAnchor
	): WebBlockAnchor? {
		if (network.findShortestPath(sourceNode, destinationNode) == null) return null

		for (nestNode in nestNodes) {
			if (network.findShortestPath(nestNode, sourceNode) == null) continue
			if (network.findShortestPath(destinationNode, nestNode) != null) return nestNode
		}

		return null
	}

	private fun getInventoryAnchors(level: ServerLevel, network: WebNetwork): List<WebBlockAnchor> {
		val anchors = mutableListOf<WebBlockAnchor>()

		for (node in network.getNodes()) {
			if (node !is WebBlockAnchor || node.blockPos == blockPos) continue
			if (!node.hasNestInterface) continue
			if (getItemHandler(level, node) == null) continue

			anchors.add(node)
		}

		return anchors
	}

	private fun getAnchors(network: WebNetwork, pos: BlockPos): List<WebBlockAnchor> {
		val anchors = mutableListOf<WebBlockAnchor>()

		for (node in network.getNodes()) {
			if (node is WebBlockAnchor && node.blockPos == pos) {
				anchors.add(node)
			}
		}

		return anchors
	}

	private fun getItemHandler(level: ServerLevel, anchor: WebBlockAnchor): IItemHandler? {
		return level.getCapability(Capabilities.ItemHandler.BLOCK, anchor.blockPos, anchor.face)
	}

	private fun getInsertableAmount(level: ServerLevel, anchor: WebBlockAnchor, stack: ItemStack): Int {
		val handler = getItemHandler(level, anchor) ?: return 0
		var remainder = stack

		for (slot in 0 until handler.slots) {
			remainder = handler.insertItem(slot, remainder, true)
			if (remainder.isEmpty) break
		}

		return stack.count - remainder.count
	}

	override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
		super.saveAdditional(tag, registries)
		tag.put(SPIDERS_TAG, saveSpiders(registries))
	}

	private fun saveSpiders(registries: HolderLookup.Provider): ListTag {
		val spidersTag = ListTag()

		for (spider in hoppingSpiders) {
			spidersTag.add(spider.save(registries))
		}

		return spidersTag
	}

	override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
		super.loadAdditional(tag, registries)
		hoppingSpiders.clear()
		loadSpiders(tag.getList(SPIDERS_TAG, CompoundTag.TAG_COMPOUND.toInt()), registries)
	}

	private fun loadSpiders(spidersTag: ListTag, registries: HolderLookup.Provider) {
		for (index in spidersTag.indices) {
			hoppingSpiders.add(HoppingSpider.load(spidersTag.getCompound(index), registries))
		}
	}

	companion object {
		private const val SPIDERS_TAG = "HoppingSpiders"
		private const val STARTING_SPIDER_COUNT = 4
		private const val MAX_TRANSFER_SIZE = 64

		fun serverTick(
			level: Level,
			pos: BlockPos,
			state: BlockState,
			blockEntity: HoppingSpiderNestBlockEntity
		) {
			if (level is ServerLevel) {
				blockEntity.serverTick(level)
			}
		}
	}
}