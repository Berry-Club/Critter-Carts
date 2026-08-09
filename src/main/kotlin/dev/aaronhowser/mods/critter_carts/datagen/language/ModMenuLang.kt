package dev.aaronhowser.mods.critter_carts.datagen.language

object ModMenuLang {

	const val NO_MENU_ACCESS = "tooltip.critter_carts.no_menu_access"
	const val SADDLE_NOT_INCLUDED = "tooltip.critter_carts.saddle_not_included"

	fun add(provider: ModLanguageProvider) {
		provider.add(NO_MENU_ACCESS, "Cannot be accessed with a menu")
		provider.add(SADDLE_NOT_INCLUDED, "Saddle not included")
	}
}