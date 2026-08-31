package dev.aaronhowser.mods.critter_carts.datagen.language

import dev.aaronhowser.mods.critter_carts.registry.ModSoundEvents

object ModSoundLang {

	fun add(provider: ModLanguageProvider) {
		provider.add(ModSoundEvents.SCOOCHWORM_FOOTSTEP_SUBTITLE, "Scoochworm scooches")
		provider.add(ModSoundEvents.SCOOCHWORM_KISS_SUBTITLE, "Scoochworm smooches")
	}
}