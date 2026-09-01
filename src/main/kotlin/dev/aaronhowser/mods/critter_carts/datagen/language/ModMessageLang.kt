package dev.aaronhowser.mods.critter_carts.datagen.language

object ModMessageLang {

	const val FIRST_NODE_MESSAGE = "item.critter_carts.web_fluid.first_node"
	const val SAME_DIRECTION_MESSAGE = "item.critter_carts.web_fluid.same_direction"
	const val TOO_LONG_MESSAGE = "item.critter_carts.web_fluid.too_long"
	const val OBSTRUCTED_MESSAGE = "item.critter_carts.web_fluid.obstructed"
	const val LINE_CREATED_MESSAGE = "item.critter_carts.web_fluid.line_created"

	fun add(provider: ModLanguageProvider) {
		provider.apply {
			add(FIRST_NODE_MESSAGE, "First web endpoint selected")
			add(SAME_DIRECTION_MESSAGE, "The selected faces cannot point in the same direction")
			add(TOO_LONG_MESSAGE, "The web line must be shorter than 10 blocks")
			add(OBSTRUCTED_MESSAGE, "The selected faces do not have line of sight")
			add(LINE_CREATED_MESSAGE, "Web line created")
		}
	}

}