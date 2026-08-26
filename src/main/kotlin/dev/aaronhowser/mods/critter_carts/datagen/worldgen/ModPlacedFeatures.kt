package dev.aaronhowser.mods.critter_carts.datagen.worldgen

import dev.aaronhowser.mods.critter_carts.CritterCarts
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.levelgen.VerticalAnchor
import net.minecraft.world.level.levelgen.placement.BiomeFilter
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement
import net.minecraft.world.level.levelgen.placement.InSquarePlacement
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import net.minecraft.world.level.levelgen.placement.RarityFilter

object ModPlacedFeatures {

	val SCOOCHWORM_APPLE: ResourceKey<PlacedFeature> = key("scoochworm_apple")

	fun bootstrap(context: BootstrapContext<PlacedFeature>) {
		val configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE)

		context.register(
			SCOOCHWORM_APPLE,
			PlacedFeature(
				configuredFeatures.getOrThrow(ModConfiguredFeatures.SCOOCHWORM_APPLE),
				listOf(
					RarityFilter.onAverageOnceEvery(24),
					InSquarePlacement.spread(),
					HeightRangePlacement.uniform(
						VerticalAnchor.bottom(),
						VerticalAnchor.absolute(64)
					),
					BiomeFilter.biome()
				)
			)
		)
	}

	private fun key(name: String): ResourceKey<PlacedFeature> {
		return ResourceKey.create(Registries.PLACED_FEATURE, CritterCarts.modResource(name))
	}
}