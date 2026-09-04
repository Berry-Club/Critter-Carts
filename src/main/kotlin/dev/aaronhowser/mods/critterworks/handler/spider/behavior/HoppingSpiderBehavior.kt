package dev.aaronhowser.mods.critterworks.handler.spider.behavior

import dev.aaronhowser.mods.critterworks.handler.spider.HoppingSpider
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3
import java.util.*

interface HoppingSpiderBehavior {

	val priority: Int
	val currentNodeUuid: UUID?
	val canBeInterrupted: Boolean

	fun tick(level: ServerLevel, spider: HoppingSpider, nestPosition: Vec3): Boolean
	fun save(): CompoundTag
}