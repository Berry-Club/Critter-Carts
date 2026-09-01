package dev.aaronhowser.mods.critter_carts.handler.web

import net.minecraft.core.BlockPos

data class WebLineInvalidation(
	val reason: WebLineInvalidationReason,
	val blockPos: BlockPos? = null
)