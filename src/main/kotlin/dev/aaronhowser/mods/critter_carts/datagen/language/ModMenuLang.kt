package dev.aaronhowser.mods.critter_carts.datagen.language

object ModMenuLang {

	const val CONTAINER_EMPTY = "tooltip.critter_carts.container.empty"
	const val CONTAINER_STACK = "tooltip.critter_carts.container.stack"
	const val CONTAINER_STACKS = "tooltip.critter_carts.container.stacks"
	const val INVERTED_ON = "tooltip.critter_carts.item_filter.inverted_on"
	const val INVERTED_OFF = "tooltip.critter_carts.item_filter.inverted_off"
	const val USE_TAGS_ON = "tooltip.critter_carts.item_filter.use_tags_on"
	const val USE_TAGS_OFF = "tooltip.critter_carts.item_filter.use_tags_off"
	const val IGNORE_DAMAGE_ON = "tooltip.critter_carts.item_filter.ignore_damage_on"
	const val IGNORE_DAMAGE_OFF = "tooltip.critter_carts.item_filter.ignore_damage_off"
	const val IGNORE_ALL_COMPONENTS_ON = "tooltip.critter_carts.item_filter.ignore_all_components_on"
	const val IGNORE_ALL_COMPONENTS_OFF = "tooltip.critter_carts.item_filter.ignore_all_components_off"

	fun add(provider: ModLanguageProvider) {
		provider.add(CONTAINER_EMPTY, "Empty")
		provider.add(CONTAINER_STACK, "Contains %s stack")
		provider.add(CONTAINER_STACKS, "Contains %s stacks")
		provider.add(INVERTED_ON, "Inverted: ON")
		provider.add(INVERTED_OFF, "Inverted: OFF")
		provider.add(USE_TAGS_ON, "Use Tags: ON")
		provider.add(USE_TAGS_OFF, "Use Tags: OFF")
		provider.add(IGNORE_DAMAGE_ON, "Ignore Damage: ON")
		provider.add(IGNORE_DAMAGE_OFF, "Ignore Damage: OFF")
		provider.add(IGNORE_ALL_COMPONENTS_ON, "Ignore All Components: ON")
		provider.add(IGNORE_ALL_COMPONENTS_OFF, "Ignore All Components: OFF")
		provider.add("menu.critter_carts.interface.color", "Color: %s")
		provider.add("menu.critter_carts.interface.input", "Input")
		provider.add("menu.critter_carts.interface.output", "Output")
		provider.add("tooltip.critter_carts.interface.color", "Color: %s")
		provider.add("tooltip.critter_carts.interface.input", "Input")
		provider.add("tooltip.critter_carts.interface.output", "Output")
	}
}