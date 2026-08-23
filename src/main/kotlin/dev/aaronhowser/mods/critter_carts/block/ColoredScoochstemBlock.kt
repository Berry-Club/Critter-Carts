package dev.aaronhowser.mods.critter_carts.block

import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import dev.aaronhowser.mods.critter_carts.entity.data.WormColor
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockState

class ColoredScoochstemBlock(
	val color: WormColor
) : RotatedPillarBlock(Properties.ofFullCopy(Blocks.OAK_LOG)), ScoochwormTravelBlock {

	override fun supportsScoochwormTravel(
		blockState: BlockState,
		scoochworm: ScoochwormEntity,
		level: Level,
		position: BlockPos,
		face: Direction
	): Boolean {
		return scoochworm.color == this.color
	}
}