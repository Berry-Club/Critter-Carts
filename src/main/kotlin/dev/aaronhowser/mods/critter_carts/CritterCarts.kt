package dev.aaronhowser.mods.critter_carts

import dev.aaronhowser.mods.critter_carts.config.ClientConfig
import dev.aaronhowser.mods.critter_carts.registry.ModRegistries
import net.minecraft.resources.ResourceLocation
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.client.gui.ConfigurationScreen
import net.neoforged.neoforge.client.gui.IConfigScreenFactory
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.runWhenOn

@Mod(CritterCarts.MOD_ID)
class CritterCarts(
	modContainer: ModContainer
) {

	init {
		ModRegistries.register(MOD_BUS)

		runWhenOn(Dist.CLIENT) {
			val screenFactory = IConfigScreenFactory { container, screen -> ConfigurationScreen(container, screen) }
			modContainer.registerExtensionPoint(IConfigScreenFactory::class.java, screenFactory)
		}

		modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG_SPEC)
	}

	companion object {
		const val MOD_ID = "critter_carts"

		@JvmField
		val LOGGER: Logger = LogManager.getLogger(MOD_ID)

		fun modResource(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
	}

}