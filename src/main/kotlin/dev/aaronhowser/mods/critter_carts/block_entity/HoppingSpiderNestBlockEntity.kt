package dev.aaronhowser.mods.critter_carts.block_entity

import dev.aaronhowser.mods.aaron.block_entity.SyncingBlockEntity
import dev.aaronhowser.mods.critter_carts.handler.web.WebNetwork
import dev.aaronhowser.mods.critter_carts.handler.web.WebSavedData
import dev.aaronhowser.mods.critter_carts.handler.web.node.BlockAnchor
import dev.aaronhowser.mods.critter_carts.handler.web.path.WebPath
import dev.aaronhowser.mods.critter_carts.registry.ModBlockEntityTypes
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.items.IItemHandler

class HoppingSpiderNestBlockEntity(
	pos: BlockPos,
	state: BlockState
) : SyncingBlockEntity(ModBlockEntityTypes.HOPPING_SPIDER_NEST.get(), pos, state) {

	override val syncImmediately: Boolean = true

	val hoppingSpiders: List<HoppingSpider>
		field = mutableListOf(HoppingSpider())

	private fun tick() {
		val level = level ?: return

		for (spider in hoppingSpiders) {
			if (spider.position == null) {
				spider.position = blockPos.center
			}

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
						val homeNode = findHomeNode(network, nestNodes, sourceNode, destinationNode)
							?: continue

						spider.homeNodeUuid = homeNode.uuid
						spider.sourceNodeUuid = sourceNode.uuid
						spider.destinationNodeUuid = destinationNode.uuid
						spider.currentNodeUuid = homeNode.uuid
						spider.targetNodeUuid = sourceNode.uuid
						spider.routeProgress = 0.0
						spider.position = homeNode.position
						spider.state = HoppingSpider.State.TO_SOURCE
						setChanged()
						return
					}
				}
			}
		}
	}

	private fun travelToSource(level: Level, spider: HoppingSpider) {
		val route = findRoute(level, spider) ?: return
		if (!advance(spider, route)) return

		val sourceHandler = getItemHandler(level, route.endNode as BlockAnchor) ?: return spider.reset()
		for (slot in 0 until sourceHandler.slots) {
			val extractedStack = sourceHandler.extractItem(slot, MAX_TRANSFER_SIZE, false)
			if (extractedStack.isEmpty) continue

			spider.carriedStack = extractedStack
			spider.currentNodeUuid = spider.sourceNodeUuid
			spider.targetNodeUuid = spider.destinationNodeUuid
			spider.routeProgress = 0.0
			spider.state = HoppingSpider.State.TO_DESTINATION
			setChanged()
			return
		}

		spider.reset()
		setChanged()
	}

	private fun travelToDestination(level: Level, spider: HoppingSpider) {
		val route = findRoute(level, spider) ?: return
		if (!advance(spider, route)) return

		val destinationHandler = getItemHandler(level, route.endNode as BlockAnchor) ?: return
		spider.carriedStack = insert(destinationHandler, spider.carriedStack, false)
		if (!spider.carriedStack.isEmpty) return

		spider.currentNodeUuid = spider.destinationNodeUuid
		spider.targetNodeUuid = spider.homeNodeUuid
		spider.routeProgress = 0.0
		spider.state = HoppingSpider.State.RETURNING
		setChanged()
	}

	private fun travelHome(level: Level, spider: HoppingSpider) {
		val route = findRoute(level, spider) ?: return
		if (!advance(spider, route)) return

		spider.reset()
		spider.position = blockPos.center
		setChanged()
	}

	private fun advance(spider: HoppingSpider, route: WebPath): Boolean {
		spider.routeProgress += TRAVEL_SPEED
		spider.position = getPosition(route, spider.routeProgress)
		setChanged()
		return spider.routeProgress >= route.distance
	}

	private fun getPosition(route: WebPath, progress: Double): Vec3 {
		if (route.segments.isEmpty()) return route.endNode.position

		var remainingDistance = progress
		for (segment in route.segments) {
			if (remainingDistance > segment.distance) {
				remainingDistance -= segment.distance
				continue
			}

			val segmentProgress = (remainingDistance / segment.distance).coerceIn(0.0, 1.0)
			return segment.fromNode.position.lerp(segment.toNode.position, segmentProgress)
		}

		return route.endNode.position
	}

	private fun findRoute(level: Level, spider: HoppingSpider): WebPath? {
		val currentNodeUuid = spider.currentNodeUuid ?: return null
		val targetNodeUuid = spider.targetNodeUuid ?: return null
		val savedData = WebSavedData.get(level.server!!.overworld())
		val currentNode = savedData.getNode(currentNodeUuid) ?: return null
		val targetNode = savedData.getNode(targetNodeUuid) ?: return null

		for (line in currentNode.lines) {
			val network = savedData.getNetwork(line.uuid) ?: continue
			val route = network.findShortestPath(currentNode, targetNode) ?: continue
			return route
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
		private const val STARTING_SPIDER_COUNT = 4
		private const val MAX_TRANSFER_SIZE = 64
		private const val TRAVEL_SPEED = 0.15

		fun serverTick(level: Level, pos: BlockPos, state: BlockState, blockEntity: HoppingSpiderNestBlockEntity) {
			blockEntity.tick()
		}
	}
}