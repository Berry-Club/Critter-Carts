package dev.aaronhowser.mods.critter_carts.datagen

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.datagen.language.ModLanguageProvider
import dev.aaronhowser.mods.critter_carts.datagen.loot.ModLootTableProvider
import dev.aaronhowser.mods.critter_carts.datagen.model.ModBlockStateProvider
import dev.aaronhowser.mods.critter_carts.datagen.tag.ModBlockTagsProvider
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.data.event.GatherDataEvent

@EventBusSubscriber(modid = CritterCarts.MOD_ID)
object ModDataGen {

	@SubscribeEvent
	fun onGatherData(event: GatherDataEvent) {
		val generator = event.generator
		val output = generator.packOutput

		generator.addProvider(
			event.includeClient(),
			ModBlockStateProvider(output, event.existingFileHelper)
		)
		generator.addProvider(
			event.includeClient(),
			ModLanguageProvider(output)
		)
		generator.addProvider(
			event.includeServer(),
			ModLootTableProvider(output, event.lookupProvider)
		)
		generator.addProvider(
			event.includeServer(),
			ModBlockTagsProvider(output, event.lookupProvider, event.existingFileHelper)
		)
	}
}