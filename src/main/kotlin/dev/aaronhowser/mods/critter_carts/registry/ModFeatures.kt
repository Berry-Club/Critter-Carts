package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.world.feature.ScoochwormAppleFeature
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.levelgen.feature.Feature
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

object ModFeatures {

	val FEATURE_REGISTRY: DeferredRegister<Feature<*>> =
		DeferredRegister.create(Registries.FEATURE, CritterCarts.MOD_ID)

	val SCOOCHWORM_APPLE: DeferredHolder<Feature<*>, ScoochwormAppleFeature> =
		FEATURE_REGISTRY.register("scoochworm_apple", ::ScoochwormAppleFeature)
}