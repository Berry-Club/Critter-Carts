package dev.aaronhowser.mods.critter_carts.handler.web

import dev.aaronhowser.mods.critter_carts.handler.web.node.WebNode

data class WebPath(
	val startNode: WebNode,
	val endNode: WebNode,
	val segments: List<WebPathSegment>,
	val distance: Double
)