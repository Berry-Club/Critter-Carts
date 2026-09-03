package dev.aaronhowser.mods.critter_carts.handler.web.line

import dev.aaronhowser.mods.critter_carts.handler.web.node.LineAnchor

data class LineAnchorAttachment(
	val anchor: LineAnchor,
	val distanceToFirstNode: Double,
	val distanceToSecondNode: Double
)