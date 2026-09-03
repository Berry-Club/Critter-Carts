package dev.aaronhowser.mods.critter_carts.block_entity

import dev.aaronhowser.mods.critter_carts.handler.web.WebNetwork
import dev.aaronhowser.mods.critter_carts.handler.web.WebSavedData
import dev.aaronhowser.mods.critter_carts.handler.web.node.BlockAnchor
import dev.aaronhowser.mods.critter_carts.registry.ModBlockEntityTypes
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.items.IItemHandler

class HoppingSpiderNestBlockEntity(
	pos: BlockPos,
	state: BlockState
) : BlockEntity(ModBlockEntityTypes.HOPPING_SPIDER_NEST.get(), pos, state) {

	val hoppingSpiders: MutableList<HoppingSpider> = mutableListOf(HoppingSpider())

	private fun tick() {
		val level = level ?: return

		for (spider in hoppingSpiders) {
			when (spider.state) {
				HoppingSpider.State.IDLE -> findWork(level, spider)
				HoppingSpider.State.TO_SOURCE -> travelToSource(level, spider)
				HoppingSpider.State.TO_DESTINATION -> travelToDestination(level, spider)
				HoppingSpider.State.RETURNING -> travelHome(level, spider)
			}
		}
	}

	private fun findWork(level: Level, spider: HoppingSpider) {
		val savedData = WebSavedData.get(level.server!!.overworld())
		for (network in savedData.getNetworksAt(blockPos)) {
			val nestNodes = getAnchors(network, blockPos)
			val inventoryNodes = getInventoryAnchors(level, network)

			for (sourceNode in inventoryNodes) {
				val sourceHandler = getItemHandler(level, sourceNode) ?: continue
				for (sourceSlot in 0 until sourceHandler.slots) {
					val extractableStack = sourceHandler.extractItem(sourceSlot, MAX_TRANSFER_SIZE, true)
					if (extractableStack.isEmpty) continue

					for (destinationNode in inventoryNodes) {
						if (destinationNode.blockPos == sourceNode.blockPos) continue
						if (!canFullyInsert(level, destinationNode, extractableStack)) continue
						if (!hasCompleteRoute(network, nestNodes, sourceNode, destinationNode)) continue

						spider.sourcePos = sourceNode.blockPos
						spider.destinationPos = destinationNode.blockPos
						spider.routeProgress = 0.0
						spider.state = HoppingSpider.State.TO_SOURCE
						setChanged()
						return
					}
				}
			}
		}
	}

	private fun travelToSource(level: Level, spider: HoppingSpider) {
		val sourcePos = spider.sourcePos ?: return spider.reset()
		val route = findRoute(level, blockPos, sourcePos) ?: return
		if (!advance(spider, route.distance)) return

		val sourceHandler = getItemHandler(level, route.endNode as BlockAnchor) ?: return spider.reset()
		for (slot in 0 until sourceHandler.slots) {
			val extractedStack = sourceHandler.extractItem(slot, MAX_TRANSFER_SIZE, false)
			if (extractedStack.isEmpty) continue

			spider.carriedStack = extractedStack
			spider.routeProgress = 0.0
			spider.state = HoppingSpider.State.TO_DESTINATION
			setChanged()
			return
		}

		spider.reset()
		setChanged()
	}

	private fun travelToDestination(level: Level, spider: HoppingSpider) {
		val sourcePos = spider.sourcePos ?: return
		val destinationPos = spider.destinationPos ?: return
		val route = findRoute(level, sourcePos, destinationPos) ?: return
		if (!advance(spider, route.distance)) return

		val destinationHandler = getItemHandler(level, route.endNode as BlockAnchor) ?: return
		spider.carriedStack = insert(destinationHandler, spider.carriedStack, false)
		if (!spider.carriedStack.isEmpty) return

		spider.routeProgress = 0.0
		spider.state = HoppingSpider.State.RETURNING
		setChanged()
	}

	private fun travelHome(level: Level, spider: HoppingSpider) {
		val destinationPos = spider.destinationPos ?: return
		val route = findRoute(level, destinationPos, blockPos) ?: return
		if (!advance(spider, route.distance)) return

		spider.reset()
		setChanged()
	}

	private fun advance(spider: HoppingSpider, distance: Double): Boolean {
		spider.routeProgress += TRAVEL_SPEED
		setChanged()
		return spider.routeProgress >= distance
	}

	private fun findRoute(level: Level, startPos: BlockPos, endPos: BlockPos) =
		WebSavedData.get(level.server!!.overworld())
			.getNetworksAt(startPos)
			.asSequence()
			.mapNotNull { network ->
				val startNodes = getUsableAnchors(level, network, startPos)
				val endNodes = getUsableAnchors(level, network, endPos)
				startNodes.asSequence()
					.flatMap { startNode ->
						endNodes.asSequence().mapNotNull { endNode ->
							network.findShortestPath(startNode, endNode)
						}
					}
					.minByOrNull { path -> path.distance }
			}
			.minByOrNull { path -> path.distance }

	private fun getUsableAnchors(level: Level, network: WebNetwork, pos: BlockPos): List<BlockAnchor> {
		val anchors = getAnchors(network, pos)
		if (pos == blockPos) return anchors

		return anchors.filter { anchor -> getItemHandler(level, anchor) != null }
	}

	private fun hasCompleteRoute(
		network: WebNetwork,
		nestNodes: List<BlockAnchor>,
		sourceNode: BlockAnchor,
		destinationNode: BlockAnchor
	): Boolean {
		val sourceToDestination = network.findShortestPath(sourceNode, destinationNode) ?: return false
		if (sourceToDestination.distance < 0.0) return false

		for (nestNode in nestNodes) {
			if (network.findShortestPath(nestNode, sourceNode) == null) continue
			if (network.findShortestPath(destinationNode, nestNode) != null) return true
		}

		return false
	}

	private fun getInventoryAnchors(level: Level, network: WebNetwork): List<BlockAnchor> {
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

	private fun getItemHandler(level: Level, anchor: BlockAnchor): IItemHandler? {
		return level.getCapability(Capabilities.ItemHandler.BLOCK, anchor.blockPos, anchor.face)
	}

	private fun canFullyInsert(level: Level, anchor: BlockAnchor, stack: ItemStack): Boolean {
		val handler = getItemHandler(level, anchor) ?: return false
		return insert(handler, stack, true).isEmpty
	}

	private fun insert(handler: IItemHandler, stack: ItemStack, simulate: Boolean): ItemStack {
		var remainder = stack
		for (slot in 0 until handler.slots) {
			remainder = handler.insertItem(slot, remainder, simulate)
			if (remainder.isEmpty) break
		}

		return remainder
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
		private const val MAX_TRANSFER_SIZE = 64
		private const val TRAVEL_SPEED = 0.15

		fun serverTick(level: Level, pos: BlockPos, state: BlockState, blockEntity: HoppingSpiderNestBlockEntity) {
			blockEntity.tick()
		}
	}
}