package dev.aaronhowser.mods.critterworks.datagen.language

import dev.aaronhowser.mods.critterworks.registry.ModMobEffects

object ModEffectLang {

	fun add(provider: ModLanguageProvider) {
		provider.addEffect(ModMobEffects.DYED_GREEN, "Dyed Green")
		provider.addEffect(ModMobEffects.DYED_BLUE, "Dyed Blue")
		provider.addEffect(ModMobEffects.DYED_RED, "Dyed Red")
		provider.addEffect(ModMobEffects.DYED_YELLOW, "Dyed Yellow")
		provider.addEffect(ModMobEffects.DYED_MAGENTA, "Dyed Magenta")
		provider.addEffect(ModMobEffects.DYED_CYAN, "Dyed Cyan")
		provider.addEffect(ModMobEffects.AARON, "Aaron.")
	}
}