package dev.aaronhowser.mods.critter_carts.block_entity

import dev.aaronhowser.mods.critter_carts.handler.web.WebSavedData
import dev.aaronhowser.mods.critter_carts.handler.web.node.BlockAnchor
import dev.aaronhowser.mods.critter_carts.handler.web.path.WebPath
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.items.IItemHandler

class HoppingSpider {

	var job: HoppingSpiderJob? = null
	var routeProgress: Double = 0.0
	var carriedStack: ItemStack = ItemStack.EMPTY
	var position: Vec3? = null

	fun tick(level: ServerLevel, nestPosition: Vec3, setChanged: () -> Unit) {
		if (position == null) {
			position = nestPosition
		}

		val job = job ?: return
		val route = findRoute(level, job) ?: return
		if (!advance(route, setChanged)) return

		when (job.phase) {
			HoppingSpiderJob.Phase.TO_SOURCE -> pickUpItem(level, job, setChanged)
			HoppingSpiderJob.Phase.TO_DESTINATION -> insertItem(level, job, setChanged)
			HoppingSpiderJob.Phase.RETURNING -> finishJob(nestPosition, setChanged)
		}
	}

	private fun pickUpItem(
		level: ServerLevel,
		job: HoppingSpiderJob,
		setChanged: () -> Unit
	) {
		val sourceNode = WebSavedData.get(level).getNode(job.sourceNodeUuid)
		if (sourceNode !is BlockAnchor) {
			cancelJob(setChanged)
			return
		}

		val sourceHandler = getItemHandler(level, sourceNode)
		if (sourceHandler == null) {
			cancelJob(setChanged)
			return
		}

		val extractedStack = sourceHandler.extractItem(job.sourceSlot, MAX_TRANSFER_SIZE, false)
		if (extractedStack.isEmpty) {
			cancelJob(setChanged)
			return
		}

		carriedStack = extractedStack
		job.phase = HoppingSpiderJob.Phase.TO_DESTINATION
		routeProgress = 0.0
		setChanged()
	}

	private fun insertItem(
		level: ServerLevel,
		job: HoppingSpiderJob,
		setChanged: () -> Unit
	) {
		val destinationNode = WebSavedData.get(level).getNode(job.destinationNodeUuid)
		if (destinationNode !is BlockAnchor) return

		val destinationHandler = getItemHandler(level, destinationNode) ?: return
		carriedStack = insert(destinationHandler, carriedStack)
		if (!carriedStack.isEmpty) return

		job.phase = HoppingSpiderJob.Phase.RETURNING
		routeProgress = 0.0
		setChanged()
	}

	private fun finishJob(nestPosition: Vec3, setChanged: () -> Unit) {
		job = null
		routeProgress = 0.0
		position = nestPosition
		setChanged()
	}

	private fun cancelJob(setChanged: () -> Unit) {
		job = null
		routeProgress = 0.0
		setChanged()
	}

	private fun findRoute(level: ServerLevel, job: HoppingSpiderJob): WebPath? {
		val savedData = WebSavedData.get(level)
		val currentNode = savedData.getNode(job.getCurrentNodeUuid()) ?: return null
		val targetNode = savedData.getNode(job.getTargetNodeUuid()) ?: return null

		for (line in currentNode.lines) {
			val network = savedData.getNetwork(line.uuid) ?: continue
			val route = network.findShortestPath(currentNode, targetNode) ?: continue
			return route
		}

		return null
	}

	private fun advance(route: WebPath, setChanged: () -> Unit): Boolean {
		routeProgress += TRAVEL_SPEED
		position = getPosition(route, routeProgress)
		setChanged()
		return routeProgress >= route.distance
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

	private fun getItemHandler(level: ServerLevel, anchor: BlockAnchor): IItemHandler? {
		return level.getCapability(Capabilities.ItemHandler.BLOCK, anchor.blockPos, anchor.face)
	}

	private fun insert(handler: IItemHandler, stack: ItemStack): ItemStack {
		var remainder = stack
		for (slot in 0 until handler.slots) {
			remainder = handler.insertItem(slot, remainder, false)
			if (remainder.isEmpty) break
		}

		return remainder
	}

	fun save(registries: HolderLookup.Provider): CompoundTag {
		val tag = CompoundTag()
		tag.putDouble(ROUTE_PROGRESS_TAG, routeProgress)

		val job = job
		if (job != null) {
			tag.put(JOB_TAG, job.save())
		}

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

	companion object {
		private const val JOB_TAG = "Job"
		private const val ROUTE_PROGRESS_TAG = "RouteProgress"
		private const val CARRIED_STACK_TAG = "CarriedStack"
		private const val POSITION_X_TAG = "PositionX"
		private const val POSITION_Y_TAG = "PositionY"
		private const val POSITION_Z_TAG = "PositionZ"
		private const val MAX_TRANSFER_SIZE = 64
		private const val TRAVEL_SPEED = 0.15

		fun load(tag: CompoundTag, registries: HolderLookup.Provider): HoppingSpider {
			val spider = HoppingSpider()
			spider.routeProgress = tag.getDouble(ROUTE_PROGRESS_TAG)

			if (tag.contains(JOB_TAG)) {
				spider.job = HoppingSpiderJob.load(tag.getCompound(JOB_TAG))
			}

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
	}
}