package dev.aaronhowser.mods.critter_carts.datagen.language

import dev.aaronhowser.mods.critter_carts.registry.ModBlocks

object ModBlockLang {

	fun add(provider: ModLanguageProvider) {
		provider.addBlock(ModBlocks.SCOOCHSTEM, "Scoochstem")
		provider.addBlock(ModBlocks.SCOOCHSTEM_WOOD, "Scoochstem Wood")
		provider.addBlock(ModBlocks.APPLE_SLICE, "Apple Slice")

		provider.addBlock(ModBlocks.GREEN_SCOOCHSTEM, "Green Scoochstem")
		provider.addBlock(ModBlocks.BLUE_SCOOCHSTEM, "Blue Scoochstem")
		provider.addBlock(ModBlocks.RED_SCOOCHSTEM, "Red Scoochstem")
		provider.addBlock(ModBlocks.YELLOW_SCOOCHSTEM, "Yellow Scoochstem")
		provider.addBlock(ModBlocks.MAGENTA_SCOOCHSTEM, "Magenta Scoochstem")
		provider.addBlock(ModBlocks.CYAN_SCOOCHSTEM, "Cyan Scoochstem")
	}
}