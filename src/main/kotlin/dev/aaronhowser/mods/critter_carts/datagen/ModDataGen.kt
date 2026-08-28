package dev.aaronhowser.mods.critter_carts.datagen

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.datagen.language.ModLanguageProvider
import dev.aaronhowser.mods.critter_carts.datagen.loot.ModLootTableProvider
import dev.aaronhowser.mods.critter_carts.datagen.model.ModBlockStateProvider
import dev.aaronhowser.mods.critter_carts.datagen.model.ModItemModelProvider
import dev.aaronhowser.mods.critter_carts.datagen.sound.ModSoundDefinitionsProvider
import dev.aaronhowser.mods.critter_carts.datagen.tag.ModBlockTagsProvider
import dev.aaronhowser.mods.critter_carts.datagen.tag.ModEntityTypeTagsProvider
import dev.aaronhowser.mods.critter_carts.datagen.tag.ModItemTagsProvider
import dev.aaronhowser.mods.critter_carts.datagen.tag.ModMobEffectTagsProvider
import dev.aaronhowser.mods.critter_carts.datagen.worldgen.ModBiomeModifiers
import dev.aaronhowser.mods.critter_carts.datagen.worldgen.ModConfiguredFeatures
import dev.aaronhowser.mods.critter_carts.datagen.worldgen.ModPlacedFeatures
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.registries.Registries
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.common.data.AdvancementProvider
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider
import net.neoforged.neoforge.data.event.GatherDataEvent
import net.neoforged.neoforge.registries.NeoForgeRegistries

@EventBusSubscriber(modid = CritterCarts.MOD_ID)
object ModDataGen {

	@SubscribeEvent
	fun onGatherData(event: GatherDataEvent) {
		val generator = event.generator
		val output = generator.packOutput

		generator.addProvider(
			event.includeServer(),
			DatapackBuiltinEntriesProvider(
				output,
				event.lookupProvider,
				RegistrySetBuilder()
					.add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap)
					.add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
					.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap),
				setOf(CritterCarts.MOD_ID)
			)
		)

		generator.addProvider(
			event.includeClient(),
			ModBlockStateProvider(output, event.existingFileHelper)
		)

		generator.addProvider(
			event.includeClient(),
			ModItemModelProvider(output, event.existingFileHelper)
		)

		generator.addProvider(
			event.includeClient(),
			ModLanguageProvider(output)
		)

		generator.addProvider(
			event.includeClient(),
			ModSoundDefinitionsProvider(output, event.existingFileHelper)
		)

		generator.addProvider(
			event.includeServer(),
			ModLootTableProvider(output, event.lookupProvider)
		)

		generator.addProvider(
			event.includeServer(),
			AdvancementProvider(
				output,
				event.lookupProvider,
				event.existingFileHelper,
				listOf(ModAdvancementSubProvider(event.lookupProvider))
			)
		)

		val blockTagProvider = generator.addProvider(
			event.includeServer(),
			ModBlockTagsProvider(output, event.lookupProvider, event.existingFileHelper)
		)

		generator.addProvider(
			event.includeServer(),
			ModItemTagsProvider(
				output,
				event.lookupProvider,
				blockTagProvider.contentsGetter(),
				event.existingFileHelper
			)
		)

		generator.addProvider(
			event.includeServer(),
			ModEntityTypeTagsProvider(
				output,
				event.lookupProvider
			)
		)

		generator.addProvider(
			event.includeServer(),
			ModMobEffectTagsProvider(
				output,
				event.lookupProvider,
				event.existingFileHelper
			)
		)
	}
}