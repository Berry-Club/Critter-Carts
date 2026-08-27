package dev.aaronhowser.mods.critter_carts.config

import net.neoforged.neoforge.common.ModConfigSpec
import org.apache.commons.lang3.tuple.Pair

class ServerConfig(
	private val builder: ModConfigSpec.Builder
) {

	lateinit var dyeberryVineReplacementChance: ModConfigSpec.DoubleValue

	init {
		general()
	}

	private fun general() {
		dyeberryVineReplacementChance = builder
			.comment("The chance that a berry-bearing cave vine is replaced with a dyeberry vine.")
			.defineInRange("dyeberryVineReplacementChance", 0.05, 0.0, 1.0)
	}

	companion object {
		private val configPair: Pair<ServerConfig, ModConfigSpec> = ModConfigSpec.Builder().configure(::ServerConfig)

		val CONFIG: ServerConfig = configPair.left
		val CONFIG_SPEC: ModConfigSpec = configPair.right
	}
}