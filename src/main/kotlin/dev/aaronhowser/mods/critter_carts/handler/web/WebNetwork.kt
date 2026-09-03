package dev.aaronhowser.mods.critter_carts.handler.web

import dev.aaronhowser.mods.critter_carts.handler.web.line.WebLine
import dev.aaronhowser.mods.critter_carts.handler.web.node.WebNode
import java.util.PriorityQueue
import java.util.UUID

class WebNetwork(
	val uuid: UUID = UUID.randomUUID()
) {

	val lines: Set<WebLine>
		field = mutableSetOf()

	private val pathCache: MutableMap<Pair<UUID, UUID>, WebPath?> = mutableMapOf()
	private var connectionsByNodeUuid: Map<UUID, List<WebPathSegment>>? = null

	internal fun addLine(line: WebLine) {
		if (lines.add(line)) {
			line.network = this
			invalidatePaths()
		}
	}

	internal fun addLines(lines: Collection<WebLine>) {
		for (line in lines) {
			addLine(line)
		}
	}

	internal fun removeLine(line: WebLine) {
		if (lines.remove(line) && line.network === this) {
			line.network = null
			invalidatePaths()
		}
	}

	fun findShortestPath(startNode: WebNode, endNode: WebNode): WebPath? {
		val cacheKey = startNode.uuid to endNode.uuid
		if (pathCache.containsKey(cacheKey)) {
			return pathCache[cacheKey]
		}

		val connections = getConnectionsByNodeUuid()
		if (startNode.uuid !in connections || endNode.uuid !in connections) {
			pathCache[cacheKey] = null
			return null
		}

		if (startNode.uuid == endNode.uuid) {
			val path = WebPath(startNode, endNode, emptyList(), 0.0)
			pathCache[cacheKey] = path
			return path
		}

		val distancesByNodeUuid: MutableMap<UUID, Double> = mutableMapOf(startNode.uuid to 0.0)
		val previousSegmentsByNodeUuid: MutableMap<UUID, WebPathSegment> = mutableMapOf()
		val pendingNodes = PriorityQueue(compareBy<Pair<Double, UUID>> { entry -> entry.first })
		pendingNodes.add(0.0 to startNode.uuid)

		while (pendingNodes.isNotEmpty()) {
			val (distance, nodeUuid) = pendingNodes.remove()
			if (distance != distancesByNodeUuid[nodeUuid]) continue
			if (nodeUuid == endNode.uuid) break

			for (segment in connections[nodeUuid].orEmpty()) {
				val nextNodeUuid = segment.toNode.uuid
				val nextDistance = distance + segment.distance
				val knownDistance = distancesByNodeUuid[nextNodeUuid]
				if (knownDistance != null && knownDistance <= nextDistance) continue

				distancesByNodeUuid[nextNodeUuid] = nextDistance
				previousSegmentsByNodeUuid[nextNodeUuid] = segment
				pendingNodes.add(nextDistance to nextNodeUuid)
			}
		}

		val distance = distancesByNodeUuid[endNode.uuid]
		if (distance == null) {
			pathCache[cacheKey] = null
			return null
		}

		val reversedSegments: MutableList<WebPathSegment> = mutableListOf()
		var currentNodeUuid = endNode.uuid
		while (currentNodeUuid != startNode.uuid) {
			val segment = previousSegmentsByNodeUuid[currentNodeUuid] ?: return null
			reversedSegments.add(segment)
			currentNodeUuid = segment.fromNode.uuid
		}

		val path = WebPath(startNode, endNode, reversedSegments.asReversed(), distance)
		cachePath(path)
		return path
	}

	internal fun clear() {
		val removedLines = lines.toList()
		for (line in removedLines) {
			removeLine(line)
		}
	}

	private fun getConnectionsByNodeUuid(): Map<UUID, List<WebPathSegment>> {
		val cachedConnections = connectionsByNodeUuid
		if (cachedConnections != null) return cachedConnections

		val connections: MutableMap<UUID, MutableList<WebPathSegment>> = mutableMapOf()
		for (line in lines) {
			val nodesByUuid: MutableMap<UUID, Pair<WebNode, Double>> = mutableMapOf(
				line.firstNode.uuid to (line.firstNode to 0.0),
				line.secondNode.uuid to (line.secondNode to line.length)
			)

			for (attachment in line.attachedAnchors) {
				nodesByUuid[attachment.anchor.uuid] =
					attachment.anchor to attachment.distanceToFirstNode
			}

			val orderedNodes = nodesByUuid.values.sortedBy { entry -> entry.second }
			for (nodeIndex in 0 until orderedNodes.lastIndex) {
				val (firstNode, firstDistance) = orderedNodes[nodeIndex]
				val (secondNode, secondDistance) = orderedNodes[nodeIndex + 1]
				val segmentDistance = secondDistance - firstDistance
				addConnection(connections, firstNode, secondNode, line, segmentDistance)
				addConnection(connections, secondNode, firstNode, line, segmentDistance)
			}
		}

		connectionsByNodeUuid = connections
		return connections
	}

	private fun addConnection(
		connections: MutableMap<UUID, MutableList<WebPathSegment>>,
		fromNode: WebNode,
		toNode: WebNode,
		line: WebLine,
		distance: Double
	) {
		val nodeConnections = connections.getOrPut(fromNode.uuid, ::mutableListOf)
		nodeConnections.add(WebPathSegment(fromNode, toNode, line, distance))
	}

	private fun cachePath(path: WebPath) {
		pathCache[path.startNode.uuid to path.endNode.uuid] = path

		val reversedSegments = path.segments
			.asReversed()
			.map { segment ->
				WebPathSegment(
					segment.toNode,
					segment.fromNode,
					segment.line,
					segment.distance
				)
			}
		pathCache[path.endNode.uuid to path.startNode.uuid] = WebPath(
			path.endNode,
			path.startNode,
			reversedSegments,
			path.distance
		)
	}

	internal fun invalidatePaths() {
		pathCache.clear()
		connectionsByNodeUuid = null
	}
}