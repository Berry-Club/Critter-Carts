package dev.aaronhowser.mods.critterworks.handler.web.line

import dev.aaronhowser.mods.critterworks.handler.web.node.WebLineAnchor

data class LineAnchorAttachment(
	val anchor: WebLineAnchor,
	val distanceToFirstNode: Double,
	val distanceToSecondNode: Double
)