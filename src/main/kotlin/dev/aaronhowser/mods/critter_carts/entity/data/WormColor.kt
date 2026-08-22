package dev.aaronhowser.mods.critter_carts.entity.data

enum class WormColor {
	GREEN,
	BLUE,
	RED,
	YELLOW,
	PURPLE,
	LIGHT_BLUE;

	fun next(): WormColor {
		return entries[(ordinal + 1) % entries.size]
	}

	companion object {
		fun fromOrdinal(ordinal: Int): WormColor {
			return entries.getOrElse(ordinal) { GREEN }
		}
	}
}