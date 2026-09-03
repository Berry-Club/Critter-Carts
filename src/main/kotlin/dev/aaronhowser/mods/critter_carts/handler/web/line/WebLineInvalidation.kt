package dev.aaronhowser.mods.critter_carts.handler.web.line

data class WebLineInvalidation(
	val reason: WebLineInvalidationReason,
	val dependencyDepth: Int = 0
)
