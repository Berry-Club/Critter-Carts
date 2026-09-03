package dev.aaronhowser.mods.critter_carts.handler.web

import dev.aaronhowser.mods.critter_carts.handler.web.line.WebLine
import dev.aaronhowser.mods.critter_carts.handler.web.node.WebNode
import dev.aaronhowser.mods.critter_carts.handler.web.path.WebPath
import dev.aaronhowser.mods.critter_carts.handler.web.path.WebPathfinder
import java.util.UUID

class WebNetwork(
	val uuid: UUID = UUID.randomUUID()
) {

	val lines: Set<WebLine>
		field = mutableSetOf()

	private val pathfinder = WebPathfinder(lines)

	internal fun addLine(line: WebLine) {
		if (lines.add(line)) {
			line.network = this
			pathfinder.invalidate()
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
			pathfinder.invalidate()
		}
	}

	fun findShortestPath(startNode: WebNode, endNode: WebNode): WebPath? {
		return pathfinder.findShortestPath(startNode, endNode)
	}

	fun getNodes(): Set<WebNode> {
		val nodes: MutableSet<WebNode> = mutableSetOf()
		for (line in lines) {
			nodes.add(line.firstNode)
			nodes.add(line.secondNode)
		}

		return nodes
	}

	internal fun clear() {
		val removedLines = lines.toList()
		for (line in removedLines) {
			removeLine(line)
		}
	}

	internal fun invalidatePaths() {
		pathfinder.invalidate()
	}
}