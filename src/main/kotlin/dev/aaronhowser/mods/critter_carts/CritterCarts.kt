package dev.aaronhowser.mods.critter_carts

import dev.aaronhowser.mods.critter_carts.registry.ModRegistries
import net.minecraft.resources.ResourceLocation
import net.neoforged.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

@Mod(CritterCarts.MOD_ID)
class CritterCarts {

	init {
		ModRegistries.register(MOD_BUS)
	}

	companion object {
		const val MOD_ID = "critter_carts"

		@JvmField
		val LOGGER: Logger = LogManager.getLogger(MOD_ID)

		fun modResource(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
	}

}