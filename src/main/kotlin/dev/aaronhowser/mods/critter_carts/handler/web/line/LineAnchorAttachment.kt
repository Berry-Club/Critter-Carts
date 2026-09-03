package dev.aaronhowser.mods.critter_carts.handler.web.line

import dev.aaronhowser.mods.critter_carts.handler.web.node.WebLineAnchor

data class LineAnchorAttachment(
	val anchor: WebLineAnchor,
	val distanceToFirstNode: Double,
	val distanceToSecondNode: Double
)