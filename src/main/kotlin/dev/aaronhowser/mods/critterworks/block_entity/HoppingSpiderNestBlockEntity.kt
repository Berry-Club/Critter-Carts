package dev.aaronhowser.mods.critterworks.block_entity

import dev.aaronhowser.mods.aaron.block_entity.SyncingBlockEntity
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isTrue
import dev.aaronhowser.mods.critterworks.handler.web.WebNetwork
import dev.aaronhowser.mods.critterworks.handler.web.WebSavedData
import dev.aaronhowser.mods.critterworks.handler.web.node.WebBlockAnchor
import dev.aaronhowser.mods.critterworks.handler.web.node.WebNode
import dev.aaronhowser.mods.critterworks.handler.web.spider.HoppingSpider
import dev.aaronhowser.mods.critterworks.handler.web.spider.behavior.transport.HoppingSpiderTransportReservations
import dev.aaronhowser.mods.critterworks.handler.web.spider.behavior.transport.HoppingSpiderTransportCandidate
import dev.aaronhowser.mods.critterworks.handler.web.spider.behavior.transport.HoppingSpiderTransportBehavior
import dev.aaronhowser.mods.critterworks.handler.web.spider.behavior.HoppingSpiderWanderBehavior
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
		var shouldSync = assignTransportBehaviors(level)

		for (spider in hoppingSpiders) {
			if (spider.activeBehavior == null) {
				startWandering(level, spider)
			}

			if (spider.serverTick(level, blockPos.center)) {
				shouldSync = true
			}
		}

		if (assignTransportBehaviors(level)) {
			shouldSync = true
		}

		if (shouldSync || hasActiveBehaviors()) {
			setChanged()
		}

		if (shouldSync) {
			level.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_CLIENTS)
		}
	}

	private fun hasActiveBehaviors(): Boolean {
		for (spider in hoppingSpiders) {
			if (spider.activeBehavior != null) return true
		}

		return false
	}

	private fun assignTransportBehaviors(level: ServerLevel): Boolean {
		val reservations = getTransportReservations(level)
		var assignedBehavior = false

		for (spider in hoppingSpiders) {
			val startingNode = getStartingNode(level, spider) ?: continue

			val transportBehavior = findTransportBehavior(level, reservations, startingNode) ?: continue

			if (!assignTransportBehavior(spider, transportBehavior)) continue
			reservations.reserve(transportBehavior)

			assignedBehavior = true
		}

		return assignedBehavior
	}

	private fun getTransportReservations(level: ServerLevel): HoppingSpiderTransportReservations {
		val reservations = HoppingSpiderTransportReservations()
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
				val transportBehavior = spider.transportBehavior ?: continue
				if (transportBehavior.phase == HoppingSpiderTransportBehavior.Phase.RETURNING) continue
				if (transportBehavior.phase == HoppingSpiderTransportBehavior.Phase.RETURNING_FROM_SOURCE) continue

				reservations.reserve(transportBehavior)
			}
		}

		return reservations
	}

	private fun assignTransportBehavior(spider: HoppingSpider, transportBehavior: HoppingSpiderTransportBehavior): Boolean {
		return spider.tryStartBehavior(transportBehavior)
	}

	private fun startWandering(level: ServerLevel, spider: HoppingSpider) {
		for (network in WebSavedData.get(level).getNetworksAt(blockPos)) {
			val homeNode = getAnchors(network, blockPos).firstOrNull() ?: continue
			spider.tryStartBehavior(HoppingSpiderWanderBehavior(homeNode.uuid))
			spider.position = homeNode.position
			return
		}
	}

	private fun getStartingNode(level: ServerLevel, spider: HoppingSpider): WebNode? {
		val activeBehavior = spider.activeBehavior
		if (activeBehavior != null && !activeBehavior.canBeInterrupted) return null

		val transportBehavior = spider.transportBehavior
		if (transportBehavior == null) {
			val currentNodeUuid = activeBehavior?.currentNodeUuid
			if (currentNodeUuid != null) return WebSavedData.get(level).getNode(currentNodeUuid)

			for (network in WebSavedData.get(level).getNetworksAt(blockPos)) {
				return getAnchors(network, blockPos).firstOrNull() ?: continue
			}

			return null
		}

		val nodeUuid = when (transportBehavior.phase) {
			HoppingSpiderTransportBehavior.Phase.RETURNING -> transportBehavior.destinationNodeUuid
			HoppingSpiderTransportBehavior.Phase.RETURNING_FROM_SOURCE -> transportBehavior.sourceNodeUuid
			else -> return null
		}

		return WebSavedData.get(level).getNode(nodeUuid)
	}

	private fun findTransportBehavior(
		level: ServerLevel,
		reservations: HoppingSpiderTransportReservations,
		startingNode: WebNode
	): HoppingSpiderTransportBehavior? {
		val savedData = WebSavedData.get(level)
		var bestCandidate: HoppingSpiderTransportCandidate? = null

		for (network in savedData.getNetworksAt(blockPos)) {
			val candidate = findTransportBehaviorInNetwork(level, network, reservations, startingNode)

			if (candidate?.isPreferredOver(bestCandidate) == true) {
				bestCandidate = candidate
			}
		}

		return bestCandidate?.behavior
	}

	private fun findTransportBehaviorInNetwork(
		level: ServerLevel,
		network: WebNetwork,
		reservations: HoppingSpiderTransportReservations,
		startingNode: WebNode
	): HoppingSpiderTransportCandidate? {
		val nestNodes = getAnchors(network, blockPos)
		val inventoryNodes = getInventoryAnchors(level, network)
		var bestCandidate: HoppingSpiderTransportCandidate? = null

		for (sourceNode in inventoryNodes) {
			val candidate = findTransportBehaviorFromSource(
				level,
				network,
				nestNodes,
				inventoryNodes,
				sourceNode,
				reservations,
				startingNode
			)

			if (candidate?.isPreferredOver(bestCandidate).isTrue()) {
				bestCandidate = candidate
			}
		}

		return bestCandidate
	}

	private fun findTransportBehaviorFromSource(
		level: ServerLevel,
		network: WebNetwork,
		nestNodes: List<WebBlockAnchor>,
		inventoryNodes: List<WebBlockAnchor>,
		sourceNode: WebBlockAnchor,
		reservations: HoppingSpiderTransportReservations,
		startingNode: WebNode
	): HoppingSpiderTransportCandidate? {
		val sourceInterface = SpiderNestInterfaceItem.getComponent(sourceNode.nestInterface)
		if (sourceInterface.transferDirection != NestInterfaceComponent.TransferDirection.INPUT) return null

		val sourceHandler = getItemHandler(level, sourceNode) ?: return null
		var bestCandidate: HoppingSpiderTransportCandidate? = null

		for (sourceSlot in 0 until sourceHandler.slots) {
			if (reservations.isSourceReserved(sourceNode.uuid, sourceSlot)) continue

			val stack = sourceHandler.extractItem(sourceSlot, MAX_TRANSFER_SIZE, true)

			if (stack.isEmpty) continue
			if (!passesFilter(sourceInterface, stack)) continue

			val candidate = findDestinationTransportBehavior(
				level,
				network,
				nestNodes,
				inventoryNodes,
				sourceNode,
				sourceSlot,
				stack,
				reservations,
				startingNode
			)

			if (candidate?.isPreferredOver(bestCandidate) == true) {
				bestCandidate = candidate
			}
		}

		return bestCandidate
	}

	private fun findDestinationTransportBehavior(
		level: ServerLevel,
		network: WebNetwork,
		nestNodes: List<WebBlockAnchor>,
		inventoryNodes: List<WebBlockAnchor>,
		sourceNode: WebBlockAnchor,
		sourceSlot: Int,
		stack: ItemStack,
		reservations: HoppingSpiderTransportReservations,
		startingNode: WebNode
	): HoppingSpiderTransportCandidate? {
		val sourceInterface = SpiderNestInterfaceItem.getComponent(sourceNode.nestInterface)
		var bestCandidate: HoppingSpiderTransportCandidate? = null

		for (destinationNode in inventoryNodes) {
			if (isSameFace(sourceNode, destinationNode)) continue

			val destinationInterface = SpiderNestInterfaceItem.getComponent(destinationNode.nestInterface)

			if (destinationInterface.transferDirection != NestInterfaceComponent.TransferDirection.OUTPUT) continue
			if (destinationInterface.color != sourceInterface.color) continue
			if (!passesFilter(destinationInterface, stack)) continue
			if (reservations.isDestinationReserved(destinationNode.uuid)) continue
			if (network.findShortestPath(sourceNode, destinationNode) == null) continue

			val transferAmount = getInsertableAmount(level, destinationNode, stack)
			if (transferAmount == 0) continue

			if (network.findShortestPath(startingNode, sourceNode) == null) continue

			val homeNode = findHomeNode(network, nestNodes, destinationNode)
				?: continue

			val transportBehavior = HoppingSpiderTransportBehavior(
				homeNode.uuid,
				sourceNode.uuid,
				destinationNode.uuid,
				sourceSlot,
				transferAmount,
				startingNode.uuid
			)

			val candidate = HoppingSpiderTransportCandidate(
				transportBehavior,
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
		destinationNode: WebBlockAnchor
	): WebBlockAnchor? {
		for (nestNode in nestNodes) {
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