package dev.aaronhowser.mods.critter_carts.handler.web.line

enum class WebLineInvalidationReason {
	INVALID_ANCHOR,
	MISSING_LINE,
	CYCLIC_DEPENDENCY,
	OBSTRUCTED
}
