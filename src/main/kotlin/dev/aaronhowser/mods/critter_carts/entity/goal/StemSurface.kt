package dev.aaronhowser.mods.critter_carts.entity.goal

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction

data class StemSurface(
	val supportPosition: BlockPos,
	val supportDirection: Direction
)