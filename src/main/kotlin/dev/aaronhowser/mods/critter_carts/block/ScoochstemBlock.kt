package dev.aaronhowser.mods.critter_carts.block

import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.RotatedPillarBlock

class ScoochstemBlock(
	properties: Properties
) : RotatedPillarBlock(properties), ScoochwormTravelBlock {

	override fun supportsScoochwormTravel(level: Level, face: Direction): Boolean = true

}