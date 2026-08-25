package dev.aaronhowser.mods.critter_carts.datagen.language

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.registry.ModBlocks
import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes
import dev.aaronhowser.mods.critter_carts.registry.ModSoundEvents
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.LanguageProvider

class ModLanguageProvider(
	output: PackOutput
) : LanguageProvider(output, CritterCarts.MOD_ID, "en_us") {

	override fun addTranslations() {
		ModItemLang.add(this)
		ModMenuLang.add(this)

		addBlock(ModBlocks.SCOOCHSTEM, "Scoochstem")
		addBlock(ModBlocks.SCOOCHSTEM_WOOD, "Scoochstem Wood")
		addBlock(ModBlocks.APPLE_SLICE, "Apple Slice")

		addBlock(ModBlocks.GREEN_SCOOCHSTEM, "Green Scoochstem")
		addBlock(ModBlocks.BLUE_SCOOCHSTEM, "Blue Scoochstem")
		addBlock(ModBlocks.RED_SCOOCHSTEM, "Red Scoochstem")
		addBlock(ModBlocks.YELLOW_SCOOCHSTEM, "Yellow Scoochstem")
		addBlock(ModBlocks.MAGENTA_SCOOCHSTEM, "Magenta Scoochstem")
		addBlock(ModBlocks.CYAN_SCOOCHSTEM, "Cyan Scoochstem")

		addEntityType(ModEntityTypes.SCOOCHWORM, "Scoochworm")
		add(ModSoundEvents.SCOOCHWORM_FOOTSTEP_SUBTITLE, "Scoochworm scooches")
	}
}