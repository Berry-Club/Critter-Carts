package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.critter_carts.CritterCarts
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.effect.MobEffect

object ModMobEffectTags {

	val DYES_ENTITY: TagKey<MobEffect> = TagKey.create(
		Registries.MOB_EFFECT,
		CritterCarts.modResource("dyes_entity")
	)
}