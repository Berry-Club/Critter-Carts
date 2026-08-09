package dev.aaronhowser.mods.critter_carts.datagen.language

import dev.aaronhowser.mods.critter_carts.registry.ModCreativeModeTabs
import dev.aaronhowser.mods.critter_carts.registry.ModItems

object ModItemLang {

	fun add(provider: ModLanguageProvider) {
		provider.apply {
			addItem(ModItems.SADDLEBAG, "Saddlebag")
			addItem(ModItems.WICKER_BASKET, "Wicker Basket")
			addItem(ModItems.SCOOCHWORM_SPAWN_EGG, "Scoochworm Spawn Egg")
			add(ModCreativeModeTabs.CREATIVE_TAB_TRANSLATION_KEY, "Critter Carts")
		}
	}
}