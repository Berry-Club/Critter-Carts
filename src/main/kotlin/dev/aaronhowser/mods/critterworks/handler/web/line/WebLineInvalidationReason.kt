package dev.aaronhowser.mods.critterworks.handler.web.line

enum class WebLineInvalidationReason {
	INVALID_ANCHOR,
	MISSING_LINE,
	CYCLIC_DEPENDENCY,
	OBSTRUCTED
}