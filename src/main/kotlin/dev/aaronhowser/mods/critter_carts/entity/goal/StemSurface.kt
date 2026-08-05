package dev.aaronhowser.mods.critter_carts.entity.goal

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction

data class StemSurface(
	val stem: BlockPos,
	val bottom: Direction
)