package dev.aaronhowser.mods.critter_carts.config

import net.neoforged.neoforge.common.ModConfigSpec
import org.apache.commons.lang3.tuple.Pair

class ClientConfig(
	private val builder: ModConfigSpec.Builder
) {

	lateinit var renderScoochwormAttachmentProbe: ModConfigSpec.BooleanValue

	init {
		general()
	}

	private fun general() {
		renderScoochwormAttachmentProbe = builder
			.comment("Render the Scoochworm attachment probe position through walls.")
			.define("renderScoochwormAttachmentProbe", false)
	}

	companion object {
		private val configPair: Pair<ClientConfig, ModConfigSpec> = ModConfigSpec.Builder().configure(::ClientConfig)

		val CONFIG: ClientConfig = configPair.left
		val CONFIG_SPEC: ModConfigSpec = configPair.right
	}
}