package dev.aaronhowser.mods.critterworks.datagen.language

object ModMenuLang {

	const val CONTAINER_EMPTY = "tooltip.critterworks.container.empty"
	const val CONTAINER_STACK = "tooltip.critterworks.container.stack"
	const val CONTAINER_STACKS = "tooltip.critterworks.container.stacks"
	const val INVERTED_ON = "tooltip.critterworks.item_filter.inverted_on"
	const val INVERTED_OFF = "tooltip.critterworks.item_filter.inverted_off"
	const val USE_TAGS_ON = "tooltip.critterworks.item_filter.use_tags_on"
	const val USE_TAGS_OFF = "tooltip.critterworks.item_filter.use_tags_off"
	const val IGNORE_DAMAGE_ON = "tooltip.critterworks.item_filter.ignore_damage_on"
	const val IGNORE_DAMAGE_OFF = "tooltip.critterworks.item_filter.ignore_damage_off"
	const val IGNORE_ALL_COMPONENTS_ON = "tooltip.critterworks.item_filter.ignore_all_components_on"
	const val IGNORE_ALL_COMPONENTS_OFF = "tooltip.critterworks.item_filter.ignore_all_components_off"

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
		provider.add("menu.critterworks.interface.color", "Color: %s")
		provider.add("menu.critterworks.interface.input", "Input")
		provider.add("menu.critterworks.interface.output", "Output")
		provider.add("menu.critterworks.interface.priority", "Priority")
		provider.add("menu.critterworks.spider_nest.title", "Hopping Spiders")
		provider.add("menu.critterworks.spider_nest.spider", "Spider %s")
		provider.add("menu.critterworks.spider_nest.position", "Position: %s, %s, %s")
		provider.add("menu.critterworks.spider_nest.idle", "Job: Idle")
		provider.add("menu.critterworks.spider_nest.wandering", "Job: Wandering")
		provider.add("menu.critterworks.spider_nest.collecting", "Job: Collecting %s items")
		provider.add("menu.critterworks.spider_nest.delivering", "Job: Delivering %s")
		provider.add("menu.critterworks.spider_nest.waiting", "Job: Waiting (%s)")
		provider.add("menu.critterworks.spider_nest.returning_item", "Job: Returning item (%s)")
		provider.add("menu.critterworks.spider_nest.returning", "Job: Returning to nest")
		provider.add("menu.critterworks.spider_nest.failure.destination_missing", "destination missing")
		provider.add("menu.critterworks.spider_nest.failure.source_missing", "source missing")
		provider.add("menu.critterworks.spider_nest.failure.destination_not_output", "output disabled")
		provider.add("menu.critterworks.spider_nest.failure.channel_changed", "channel changed")
		provider.add("menu.critterworks.spider_nest.failure.filter_changed", "filter changed")
		provider.add("menu.critterworks.spider_nest.failure.destination_unavailable", "inventory unavailable")
		provider.add("menu.critterworks.spider_nest.failure.destination_full", "destination full")
		provider.add("tooltip.critterworks.interface.color", "Color: %s")
		provider.add("tooltip.critterworks.interface.input", "Input")
		provider.add("tooltip.critterworks.interface.output", "Output")
		provider.add("tooltip.critterworks.interface.priority", "Priority: %s")
	}
}