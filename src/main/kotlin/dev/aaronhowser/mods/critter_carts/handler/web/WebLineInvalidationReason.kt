package dev.aaronhowser.mods.critter_carts.handler.web

enum class WebLineInvalidationReason {
	INVALID_ANCHOR,
	MISSING_LINE,
	CYCLIC_DEPENDENCY,
	OBSTRUCTED
}