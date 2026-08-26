package dev.aaronhowser.mods.critter_carts.effect

import dev.aaronhowser.mods.critter_carts.entity.data.WormColor
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory

class DyedMobEffect(
	val wormColor: WormColor
) : MobEffect(
	MobEffectCategory.NEUTRAL,
	wormColor.tintColor
)