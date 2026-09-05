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
	const val WEB_PORT_COLOR = "menu.critterworks.web_port.color"
	const val WEB_PORT_INPUT = "menu.critterworks.web_port.input"
	const val WEB_PORT_OUTPUT = "menu.critterworks.web_port.output"
	const val WEB_PORT_PRIORITY = "menu.critterworks.web_port.priority"
	const val WEB_PORT_COLOR_TOOLTIP = "tooltip.critterworks.web_port.color"
	const val WEB_PORT_INPUT_TOOLTIP = "tooltip.critterworks.web_port.input"
	const val WEB_PORT_OUTPUT_TOOLTIP = "tooltip.critterworks.web_port.output"
	const val WEB_PORT_PRIORITY_TOOLTIP = "tooltip.critterworks.web_port.priority"
	const val SPIDER_NEST_TITLE = "menu.critterworks.spider_nest.title"
	const val SPIDER_NEST_SPIDER = "menu.critterworks.spider_nest.spider"
	const val SPIDER_NEST_POSITION = "menu.critterworks.spider_nest.position"
	const val SPIDER_NEST_IDLE = "menu.critterworks.spider_nest.idle"
	const val SPIDER_NEST_WANDERING = "menu.critterworks.spider_nest.wandering"
	const val SPIDER_NEST_COLLECTING = "menu.critterworks.spider_nest.collecting"
	const val SPIDER_NEST_DELIVERING = "menu.critterworks.spider_nest.delivering"
	const val SPIDER_NEST_WAITING = "menu.critterworks.spider_nest.waiting"
	const val SPIDER_NEST_RETURNING_ITEM = "menu.critterworks.spider_nest.returning_item"
	const val SPIDER_NEST_RETURNING = "menu.critterworks.spider_nest.returning"
	const val SPIDER_NEST_FAILURE = "menu.critterworks.spider_nest.failure"

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
		provider.add(WEB_PORT_COLOR, "Color: %s")
		provider.add(WEB_PORT_INPUT, "Input")
		provider.add(WEB_PORT_OUTPUT, "Output")
		provider.add(WEB_PORT_PRIORITY, "Priority")
		provider.add(SPIDER_NEST_TITLE, "Hopping Spiders")
		provider.add(SPIDER_NEST_SPIDER, "Spider %s")
		provider.add(SPIDER_NEST_POSITION, "Position: %s, %s, %s")
		provider.add(SPIDER_NEST_IDLE, "Job: Idle")
		provider.add(SPIDER_NEST_WANDERING, "Job: Wandering")
		provider.add(SPIDER_NEST_COLLECTING, "Job: Collecting %s items")
		provider.add(SPIDER_NEST_DELIVERING, "Job: Delivering %s")
		provider.add(SPIDER_NEST_WAITING, "Job: Waiting (%s)")
		provider.add(SPIDER_NEST_RETURNING_ITEM, "Job: Returning item (%s)")
		provider.add(SPIDER_NEST_RETURNING, "Job: Returning to nest")
		provider.add("$SPIDER_NEST_FAILURE.destination_missing", "destination missing")
		provider.add("$SPIDER_NEST_FAILURE.source_missing", "source missing")
		provider.add("$SPIDER_NEST_FAILURE.destination_not_output", "output disabled")
		provider.add("$SPIDER_NEST_FAILURE.channel_changed", "channel changed")
		provider.add("$SPIDER_NEST_FAILURE.filter_changed", "filter changed")
		provider.add("$SPIDER_NEST_FAILURE.destination_unavailable", "inventory unavailable")
		provider.add("$SPIDER_NEST_FAILURE.destination_full", "destination full")
		provider.add(WEB_PORT_COLOR_TOOLTIP, "Color: %s")
		provider.add(WEB_PORT_INPUT_TOOLTIP, "Input")
		provider.add(WEB_PORT_OUTPUT_TOOLTIP, "Output")
		provider.add(WEB_PORT_PRIORITY_TOOLTIP, "Priority: %s")
	}
}