package dev.aaronhowser.mods.critter_carts.datagen.language

object ModMenuLang {

	const val CONTAINER_EMPTY = "tooltip.critter_carts.container.empty"
	const val CONTAINER_STACK = "tooltip.critter_carts.container.stack"
	const val CONTAINER_STACKS = "tooltip.critter_carts.container.stacks"

	fun add(provider: ModLanguageProvider) {
		provider.add(CONTAINER_EMPTY, "Empty")
		provider.add(CONTAINER_STACK, "Contains %s stack")
		provider.add(CONTAINER_STACKS, "Contains %s stacks")
	}
}