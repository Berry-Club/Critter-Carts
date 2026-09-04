package dev.aaronhowser.mods.critterworks.registry

import dev.aaronhowser.mods.critterworks.Critterworks
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
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
				.title(Component.translatable(CREATIVE_TAB_TRANSLATION_KEY))
				.icon(ModBlocks.SCOOCHSTEM::toStack)
				.displayItems { _, output ->
					for (item in ModItems.ITEM_REGISTRY.entries) {
						output.accept(item.get())
					}
				}
				.build()
		})
}