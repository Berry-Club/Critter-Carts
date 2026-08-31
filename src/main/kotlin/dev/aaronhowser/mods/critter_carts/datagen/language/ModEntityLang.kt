package dev.aaronhowser.mods.critter_carts.datagen.language

import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes

object ModEntityLang {

	fun add(provider: ModLanguageProvider) {
		provider.addEntityType(ModEntityTypes.SCOOCHWORM, "Scoochworm")
	}
}