package dev.aaronhowser.mods.critter_carts.entity.data

import dev.aaronhowser.mods.critter_carts.CritterCarts

enum class WormColor(
	val colorName: String,
	val tintColor: Int
) {
	GREEN("green", 0xFF55FF55.toInt()),
	BLUE("blue", 0xFF5555FF.toInt()),
	RED("red", 0xFFFF5555.toInt()),
	YELLOW("yellow", 0xFFFFFF55.toInt()),
	MAGENTA("magenta", 0xFFFF55FF.toInt()),
	CYAN("cyan", 0xFF55FFFF.toInt());

	val headTexture = CritterCarts.modResource("textures/entity/scoochworm/$colorName/head.png")
	val bodyTexture = CritterCarts.modResource("textures/entity/scoochworm/$colorName/body.png")

	fun next(): WormColor {
		return entries[(ordinal + 1) % entries.size]
	}

	companion object {
		fun fromOrdinal(ordinal: Int): WormColor {
			return entries.getOrElse(ordinal) { GREEN }
		}
	}
}