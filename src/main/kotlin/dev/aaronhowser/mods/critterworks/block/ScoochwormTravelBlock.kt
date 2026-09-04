package dev.aaronhowser.mods.critterworks.block

import dev.aaronhowser.mods.critterworks.entity.ScoochwormEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

interface ScoochwormTravelBlock {
	fun supportsScoochwormTravel(
		blockState: BlockState,
		scoochworm: ScoochwormEntity,
		level: Level,
		position: BlockPos,
		face: Direction
	): Boolean
}