package dev.aaronhowser.mods.critter_carts.handler.web

import dev.aaronhowser.mods.critter_carts.handler.web.line.WebLine
import dev.aaronhowser.mods.critter_carts.handler.web.node.WebNode

data class WebPathSegment(
	val fromNode: WebNode,
	val toNode: WebNode,
	val line: WebLine,
	val distance: Double
)