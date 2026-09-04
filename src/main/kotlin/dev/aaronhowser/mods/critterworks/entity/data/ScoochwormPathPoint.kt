package dev.aaronhowser.mods.critterworks.entity.data

import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3

data class ScoochwormPathPoint(
	val position: Vec3,
	val supportDirection: Direction
)