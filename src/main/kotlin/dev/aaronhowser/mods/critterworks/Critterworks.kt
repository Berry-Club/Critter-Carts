package dev.aaronhowser.mods.critterworks

import dev.aaronhowser.mods.critterworks.config.ClientConfig
import dev.aaronhowser.mods.critterworks.config.ServerConfig
import dev.aaronhowser.mods.critterworks.registry.ModMenuTypes
import dev.aaronhowser.mods.critterworks.registry.ModRegistries
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

@Mod(Critterworks.MOD_ID)
class Critterworks(
	modContainer: ModContainer
) {

	init {
		ModRegistries.register(MOD_BUS)

		runWhenOn(Dist.CLIENT) {
			MOD_BUS.addListener(ModMenuTypes::registerScreens)
			val screenFactory = IConfigScreenFactory { container, screen -> ConfigurationScreen(container, screen) }
			modContainer.registerExtensionPoint(IConfigScreenFactory::class.java, screenFactory)
		}

		modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG_SPEC)
		modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG_SPEC)
	}

	companion object {
		const val MOD_ID = "critterworks"

		@JvmField
		val LOGGER: Logger = LogManager.getLogger(MOD_ID)

		@JvmStatic
		fun modResource(path: String): ResourceLocation =
			ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
	}

}