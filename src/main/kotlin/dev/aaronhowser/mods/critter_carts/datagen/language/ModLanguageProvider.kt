package dev.aaronhowser.mods.critter_carts.datagen.language

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.registry.ModBlocks
import dev.aaronhowser.mods.critter_carts.registry.ModCreativeModeTabs
import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes
import dev.aaronhowser.mods.critter_carts.registry.ModItems
import dev.aaronhowser.mods.critter_carts.registry.ModSoundEvents
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.LanguageProvider

class ModLanguageProvider(
	output: PackOutput
) : LanguageProvider(output, CritterCarts.MOD_ID, "en_us") {

	override fun addTranslations() {
		addBlock(ModBlocks.SCOOCHSTEM, "Scoochstem")
		addEntityType(ModEntityTypes.SCOOCHWORM, "Scoochworm")
		addItem(ModItems.SCOOCHWORM_SPAWN_EGG, "Scoochworm Spawn Egg")
		add(ModSoundEvents.SCOOCHWORM_FOOTSTEP_SUBTITLE, "Scoochworm scooches")
		add(ModCreativeModeTabs.CREATIVE_TAB_TRANSLATION_KEY, "Critter Carts")
	}
}