package dev.aaronhowser.mods.critterworks.registry

import dev.aaronhowser.mods.critterworks.Critterworks
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.effect.MobEffect

object ModMobEffectTags {

	val DYES_ENTITY: TagKey<MobEffect> = TagKey.create(
		Registries.MOB_EFFECT,
		Critterworks.modResource("dyes_entity")
	)
}