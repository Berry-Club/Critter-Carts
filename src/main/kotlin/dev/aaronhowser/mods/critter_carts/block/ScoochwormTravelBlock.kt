package dev.aaronhowser.mods.critter_carts.block

import net.minecraft.core.Direction
import net.minecraft.world.level.Level

interface ScoochwormTravelBlock {
	fun supportsScoochwormTravel(level: Level, face: Direction): Boolean
}