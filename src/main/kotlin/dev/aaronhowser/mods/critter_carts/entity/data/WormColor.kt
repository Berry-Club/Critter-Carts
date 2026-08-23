package dev.aaronhowser.mods.critter_carts.entity.data

import dev.aaronhowser.mods.critter_carts.CritterCarts

enum class WormColor(val color: String) {
	GREEN("green"),
	BLUE("blue"),
	RED("red"),
	YELLOW("yellow"),
	PURPLE("purple"),
	CYAN("cyan");

	val headTexture = CritterCarts.modResource("textures/entity/scoochworm/$color/head.png")
	val bodyTexture = CritterCarts.modResource("textures/entity/scoochworm/$color/body.png")

	fun next(): WormColor {
		return entries[(ordinal + 1) % entries.size]
	}

	companion object {
		fun fromOrdinal(ordinal: Int): WormColor {
			return entries.getOrElse(ordinal) { GREEN }
		}
	}
}