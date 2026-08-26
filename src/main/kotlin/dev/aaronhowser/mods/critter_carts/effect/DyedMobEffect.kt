package dev.aaronhowser.mods.critter_carts.effect

import dev.aaronhowser.mods.critter_carts.entity.data.WormColor
import dev.aaronhowser.mods.critter_carts.registry.ModMobEffectTags
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.LivingEntity

class DyedMobEffect(
	val wormColor: WormColor
) : MobEffect(
	MobEffectCategory.NEUTRAL,
	wormColor.tintColor
) {

	override fun onEffectStarted(
		livingEntity: LivingEntity,
		amplifier: Int
	) {
		super.onEffectStarted(livingEntity, amplifier)

		val activeEffects = livingEntity.activeEffects.toList()

		for (effectInstance in activeEffects) {
			val effect = effectInstance.effect

			if (effect.value() === this) continue
			if (!effect.`is`(ModMobEffectTags.DYES_ENTITY)) continue

			livingEntity.removeEffect(effect)
		}
	}
}