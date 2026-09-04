package dev.aaronhowser.mods.critterworks.handler.web.line

data class WebLineInvalidation(
	val reason: WebLineInvalidationReason,
	val dependencyDepth: Int = 0
)