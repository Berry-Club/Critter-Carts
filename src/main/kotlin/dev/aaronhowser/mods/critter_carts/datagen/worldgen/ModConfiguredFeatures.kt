package dev.aaronhowser.mods.critter_carts.datagen.worldgen

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.registry.ModFeatures
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration

object ModConfiguredFeatures {

	val SCOOCHWORM_APPLE: ResourceKey<ConfiguredFeature<*, *>> = key("scoochworm_apple")

	fun bootstrap(context: BootstrapContext<ConfiguredFeature<*, *>>) {
		context.register(
			SCOOCHWORM_APPLE,
			ConfiguredFeature(
				ModFeatures.SCOOCHWORM_APPLE.get(),
				FeatureConfiguration.NONE
			)
		)
	}

	private fun key(name: String): ResourceKey<ConfiguredFeature<*, *>> {
		return ResourceKey.create(Registries.CONFIGURED_FEATURE, CritterCarts.modResource(name))
	}
}