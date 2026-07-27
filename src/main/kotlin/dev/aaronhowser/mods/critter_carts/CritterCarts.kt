package dev.aaronhowser.mods.critter_carts

import net.minecraft.resources.ResourceLocation
import net.neoforged.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(CritterCarts.MOD_ID)
class CritterCarts {

	companion object {
		const val MOD_ID = "critter_carts"

		@JvmField
		val LOGGER: Logger = LogManager.getLogger(MOD_ID)

		fun modResource(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
	}

}