package dev.aaronhowser.mods.critter_carts.handler.web.path

import dev.aaronhowser.mods.critter_carts.handler.web.line.WebLine
import dev.aaronhowser.mods.critter_carts.handler.web.node.WebNode

data class WebPathSegment(
	val fromNode: WebNode,
	val toNode: WebNode,
	val line: WebLine,
	val distance: Double
) {

	fun reversed(): WebPathSegment {
		return WebPathSegment(toNode, fromNode, line, distance)
	}

}