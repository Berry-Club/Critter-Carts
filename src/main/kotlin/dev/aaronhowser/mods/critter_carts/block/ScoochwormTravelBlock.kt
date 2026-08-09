package dev.aaronhowser.mods.critter_carts.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

interface ScoochwormTravelBlock {
	fun supportsScoochwormTravel(
		blockState: BlockState,
		level: Level,
		position: BlockPos,
		face: Direction
	): Boolean
}