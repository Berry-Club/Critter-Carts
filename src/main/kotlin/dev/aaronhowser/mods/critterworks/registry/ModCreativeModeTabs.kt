package dev.aaronhowser.mods.critterworks.registry

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.toComponent
import dev.aaronhowser.mods.critterworks.Critterworks
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.CreativeModeTab
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object ModCreativeModeTabs {

	const val CREATIVE_TAB_TRANSLATION_KEY = "itemGroup.critterworks"

	val TABS_REGISTRY: DeferredRegister<CreativeModeTab> =
		DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, Critterworks.MOD_ID)

	val MOD_TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> =
		TABS_REGISTRY.register("creative_tab", Supplier {
			CreativeModeTab.builder()
				.title(CREATIVE_TAB_TRANSLATION_KEY.toComponent())
				.icon(ModBlocks.SCOOCHSTEM::toStack)
				.displayItems { _, output ->
					for (item in ModItems.ITEM_REGISTRY.entries) {
						output.accept(item.get())
					}
				}
				.build()
		})
}