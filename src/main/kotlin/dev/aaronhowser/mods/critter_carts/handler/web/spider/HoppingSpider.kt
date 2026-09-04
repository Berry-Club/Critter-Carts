package dev.aaronhowser.mods.critter_carts.handler.web.spider

import dev.aaronhowser.mods.critter_carts.handler.web.WebSavedData
import dev.aaronhowser.mods.critter_carts.handler.web.node.WebBlockAnchor
import dev.aaronhowser.mods.critter_carts.handler.web.path.WebPath
import dev.aaronhowser.mods.critter_carts.item.ItemFilterItem
import dev.aaronhowser.mods.critter_carts.item.SpiderNestInterfaceItem
import dev.aaronhowser.mods.critter_carts.item.component.NestInterfaceComponent
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.Containers
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.items.IItemHandler
import java.util.*

class HoppingSpider(
	val uuid: UUID = UUID.randomUUID()
) {

	var job: HoppingSpiderJob? = null
	var carriedStack: ItemStack = ItemStack.EMPTY
	var position: Vec3? = null
	var route: HoppingSpiderRoute? = null
	var routeProgress: Double = 0.0

	fun serverTick(level: ServerLevel, nestPosition: Vec3): Boolean {
		if (position == null) {
			position = nestPosition
		}

		val job = job ?: return false
		val path = findPath(level, job)
		if (path == null) return handleMissingPath(level)

		val routeChanged = updateRoute(level, job, path)
		if (!moveAlong(path)) return routeChanged

		return completeCurrentLeg(level, job, nestPosition)
	}

	private fun findPath(level: ServerLevel, job: HoppingSpiderJob): WebPath? {
		val savedData = WebSavedData.get(level)
		val currentNode = savedData.getNode(job.currentNodeUuid) ?: return null
		val targetNode = savedData.getNode(job.targetNodeUuid) ?: return null

		for (line in currentNode.lines) {
			val network = savedData.getNetwork(line.uuid) ?: continue
			return network.findShortestPath(currentNode, targetNode) ?: continue
		}

		return null
	}

	private fun updateRoute(
		level: ServerLevel,
		job: HoppingSpiderJob,
		path: WebPath
	): Boolean {
		val newRoute = HoppingSpiderRoute.fromPath(path, level.gameTime, TRAVEL_SPEED)
		val route = route
		if (route != null && route.matches(job.targetNodeUuid, newRoute.positions)) return false

		this.route = newRoute
		routeProgress = 0.0
		position = newRoute.positions.first()
		return true
	}

	private fun moveAlong(path: WebPath): Boolean {
		routeProgress += TRAVEL_SPEED
		position = getPathPosition(path, routeProgress)
		return routeProgress >= path.distance
	}

	private fun getPathPosition(path: WebPath, progress: Double): Vec3 {
		if (path.segments.isEmpty()) return path.endNode.position

		var remainingDistance = progress
		for (segment in path.segments) {
			if (remainingDistance > segment.distance) {
				remainingDistance -= segment.distance
				continue
			}

			val segmentProgress = remainingDistance / segment.distance
			return segment.fromNode.position.lerp(segment.toNode.position, segmentProgress)
		}

		return path.endNode.position
	}

	private fun completeCurrentLeg(
		level: ServerLevel,
		job: HoppingSpiderJob,
		nestPosition: Vec3
	): Boolean {
		return when (job.phase) {
			HoppingSpiderJob.Phase.TO_SOURCE -> {
				pickUpItem(level, job)
				true
			}

			HoppingSpiderJob.Phase.TO_DESTINATION -> deliverItem(level, job)

			HoppingSpiderJob.Phase.RETURNING_ITEM -> {
				returnItem(level, job)
				true
			}

			HoppingSpiderJob.Phase.RETURNING -> {
				finishJob(nestPosition)
				true
			}

			HoppingSpiderJob.Phase.RETURNING_FROM_SOURCE -> {
				finishJob(nestPosition)
				true
			}
		}
	}

	private fun pickUpItem(level: ServerLevel, job: HoppingSpiderJob) {
		val source = getBlockAnchor(level, job.sourceNodeUuid)
		if (source == null) {
			cancelJob()
			return
		}

		val nestInterface = SpiderNestInterfaceItem.getComponent(source.nestInterface)

		if (nestInterface.transferDirection != NestInterfaceComponent.TransferDirection.INPUT) {
			cancelJob()
			return
		}

		val handler = getItemHandler(level, source)
		if (handler == null) {
			cancelJob()
			return
		}

		val filter = nestInterface.getFilter()
		val stack = handler.extractItem(job.sourceSlot, job.transferAmount, true)

		if (stack.isEmpty) {
			cancelJob()
			return
		}

		if (!filter.isEmpty && !ItemFilterItem.passesFilter(filter, stack)) {
			cancelJob()
			return
		}

		val extracted = handler.extractItem(job.sourceSlot, job.transferAmount, false)
		if (extracted.isEmpty) {
			cancelJob()
			return
		}

		carriedStack = extracted
		startNextLeg(HoppingSpiderJob.Phase.TO_DESTINATION)
	}

	private fun deliverItem(level: ServerLevel, job: HoppingSpiderJob): Boolean {
		val destination = getBlockAnchor(level, job.destinationNodeUuid)
			?: return failDelivery(level, job, HoppingSpiderJob.FailureReason.DESTINATION_MISSING)

		val destinationInterface = SpiderNestInterfaceItem.getComponent(destination.nestInterface)

		if (destinationInterface.transferDirection != NestInterfaceComponent.TransferDirection.OUTPUT) {
			return failDelivery(level, job, HoppingSpiderJob.FailureReason.DESTINATION_NOT_OUTPUT)
		}

		val source = getBlockAnchor(level, job.sourceNodeUuid)
			?: return failDelivery(level, job, HoppingSpiderJob.FailureReason.SOURCE_MISSING)

		val sourceInterface = SpiderNestInterfaceItem.getComponent(source.nestInterface)

		if (sourceInterface.color != destinationInterface.color) {
			return failDelivery(level, job, HoppingSpiderJob.FailureReason.CHANNEL_CHANGED)
		}

		val filter = destinationInterface.getFilter()
		if (!filter.isEmpty && !ItemFilterItem.passesFilter(filter, carriedStack)) {
			return failDelivery(level, job, HoppingSpiderJob.FailureReason.FILTER_CHANGED)
		}

		val handler = getItemHandler(level, destination)
			?: return failDelivery(level, job, HoppingSpiderJob.FailureReason.DESTINATION_UNAVAILABLE)

		carriedStack = insertItem(handler, carriedStack)
		if (!carriedStack.isEmpty) {
			return failDelivery(level, job, HoppingSpiderJob.FailureReason.DESTINATION_FULL)
		}

		job.failureReason = null
		startNextLeg(HoppingSpiderJob.Phase.RETURNING)

		return true
	}

	private fun failDelivery(
		level: ServerLevel,
		job: HoppingSpiderJob,
		reason: HoppingSpiderJob.FailureReason
	): Boolean {
		val reasonChanged = job.failureReason != reason
		job.failureReason = reason

		if (reason.shouldRetry) return reasonChanged

		if (reason == HoppingSpiderJob.FailureReason.DESTINATION_MISSING
			|| reason == HoppingSpiderJob.FailureReason.SOURCE_MISSING
		) {
			dropCarriedItem(level)
			cancelJob()
			return true
		}

		startNextLeg(HoppingSpiderJob.Phase.RETURNING_ITEM)

		return true
	}

	private fun returnItem(level: ServerLevel, job: HoppingSpiderJob) {
		val source = getBlockAnchor(level, job.sourceNodeUuid)
		if (source == null) {
			dropCarriedItem(level)
			cancelJob()
			return
		}

		val handler = getItemHandler(level, source)
		if (handler == null) {
			dropCarriedItem(level)
			startNextLeg(HoppingSpiderJob.Phase.RETURNING_FROM_SOURCE)
			return
		}

		carriedStack = returnToSource(handler, job.sourceSlot, carriedStack)

		if (!carriedStack.isEmpty) {
			dropCarriedItem(level)
		}

		startNextLeg(HoppingSpiderJob.Phase.RETURNING_FROM_SOURCE)
	}

	private fun returnToSource(handler: IItemHandler, sourceSlot: Int, stack: ItemStack): ItemStack {
		var remainder = stack

		if (sourceSlot in 0 until handler.slots) {
			remainder = handler.insertItem(sourceSlot, remainder, false)
		}

		for (slot in 0 until handler.slots) {
			if (slot == sourceSlot) continue

			remainder = handler.insertItem(slot, remainder, false)
			if (remainder.isEmpty) break
		}

		return remainder
	}

	private fun handleMissingPath(level: ServerLevel): Boolean {
		if (carriedStack.isEmpty) return false

		dropCarriedItem(level)
		cancelJob()

		return true
	}

	private fun dropCarriedItem(level: ServerLevel) {
		val position = position ?: return

		Containers.dropItemStack(
			level,
			position.x,
			position.y,
			position.z,
			carriedStack
		)

		carriedStack = ItemStack.EMPTY
	}

	private fun startNextLeg(phase: HoppingSpiderJob.Phase) {
		val job = job ?: return
		job.phase = phase
		route = null
		routeProgress = 0.0
	}

	private fun finishJob(nestPosition: Vec3) {
		job = null
		position = nestPosition
		route = null
		routeProgress = 0.0
	}

	private fun cancelJob() {
		job = null
		route = null
		routeProgress = 0.0
	}

	private fun getBlockAnchor(level: ServerLevel, uuid: UUID): WebBlockAnchor? {
		return WebSavedData.get(level).getNode(uuid) as? WebBlockAnchor
	}

	private fun getItemHandler(level: ServerLevel, anchor: WebBlockAnchor): IItemHandler? {
		if (!anchor.hasNestInterface) return null
		return level.getCapability(Capabilities.ItemHandler.BLOCK, anchor.blockPos, anchor.face)
	}

	private fun insertItem(handler: IItemHandler, stack: ItemStack): ItemStack {
		var remainder = stack
		for (slot in 0 until handler.slots) {
			remainder = handler.insertItem(slot, remainder, false)
			if (remainder.isEmpty) break
		}

		return remainder
	}

	fun getRenderPosition(gameTime: Long, partialTick: Float): Vec3? {
		return route?.getPosition(gameTime, partialTick) ?: position
	}

	fun save(registries: HolderLookup.Provider): CompoundTag {
		val tag = CompoundTag()
		tag.putUUID(UUID_TAG, uuid)
		tag.putDouble(ROUTE_PROGRESS_TAG, routeProgress)

		val job = job
		if (job != null) {
			tag.put(JOB_TAG, job.save())
		}

		val route = route
		if (route != null) {
			tag.put(ROUTE_TAG, route.save())
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
		private const val UUID_TAG = "Uuid"
		private const val JOB_TAG = "Job"
		private const val ROUTE_TAG = "Route"
		private const val ROUTE_PROGRESS_TAG = "RouteProgress"
		private const val CARRIED_STACK_TAG = "CarriedStack"
		private const val POSITION_X_TAG = "PositionX"
		private const val POSITION_Y_TAG = "PositionY"
		private const val POSITION_Z_TAG = "PositionZ"
		private const val TRAVEL_SPEED = 0.15

		fun load(tag: CompoundTag, registries: HolderLookup.Provider): HoppingSpider {
			val uuid = getUuid(tag)
			val spider = HoppingSpider(uuid)
			spider.job = getJob(tag)
			spider.route = getRoute(tag)
			spider.routeProgress = tag.getDouble(ROUTE_PROGRESS_TAG)
			spider.carriedStack = getCarriedStack(tag, registries)
			spider.position = getPosition(tag)
			return spider
		}

		private fun getUuid(tag: CompoundTag): UUID {
			if (tag.hasUUID(UUID_TAG)) return tag.getUUID(UUID_TAG)
			return UUID.randomUUID()
		}

		private fun getJob(tag: CompoundTag): HoppingSpiderJob? {
			if (!tag.contains(JOB_TAG)) return null
			return HoppingSpiderJob.load(tag.getCompound(JOB_TAG))
		}

		private fun getRoute(tag: CompoundTag): HoppingSpiderRoute? {
			if (!tag.contains(ROUTE_TAG)) return null
			return HoppingSpiderRoute.load(tag.getCompound(ROUTE_TAG))
		}

		private fun getCarriedStack(
			tag: CompoundTag,
			registries: HolderLookup.Provider
		): ItemStack {
			return ItemStack.parseOptional(registries, tag.getCompound(CARRIED_STACK_TAG))
		}

		private fun getPosition(tag: CompoundTag): Vec3? {
			if (!tag.contains(POSITION_X_TAG)) return null

			return Vec3(
				tag.getDouble(POSITION_X_TAG),
				tag.getDouble(POSITION_Y_TAG),
				tag.getDouble(POSITION_Z_TAG)
			)
		}
	}
}