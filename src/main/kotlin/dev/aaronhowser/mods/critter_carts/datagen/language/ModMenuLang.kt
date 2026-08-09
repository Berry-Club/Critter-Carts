package dev.aaronhowser.mods.critter_carts.datagen.language

object ModMenuLang {

	const val SADDLE_NOT_INCLUDED = "tooltip.critter_carts.saddle_not_included"

	fun add(provider: ModLanguageProvider) {
		provider.add(SADDLE_NOT_INCLUDED, "Saddle not included")
	}
}