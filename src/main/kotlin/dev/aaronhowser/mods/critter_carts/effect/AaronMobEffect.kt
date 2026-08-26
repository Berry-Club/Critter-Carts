package dev.aaronhowser.mods.critter_carts.effect

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isHolder
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.entity.data.WormColor
import dev.aaronhowser.mods.critter_carts.registry.ModMobEffectTags
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.LivingEntity

class AaronMobEffect : MobEffect(
	MobEffectCategory.NEUTRAL,
	WormColor.AARON.tintColor
) {

	override fun onEffectStarted(
		livingEntity: LivingEntity,
		amplifier: Int
	) {
		val activeEffects = livingEntity.activeEffects.toList()

		for (effectInstance in activeEffects) {
			val effect = effectInstance.effect

			if (effect.value() === this) continue
			if (!effect.isHolder(ModMobEffectTags.DYES_ENTITY)) continue

			livingEntity.removeEffect(effect)
		}
	}

	companion object {
		@JvmField
		val SKIN: ResourceLocation =
			CritterCarts.modResource("textures/entity/player/aaron.png")
	}
}