package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.critter_carts.CritterCarts
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object ModCreativeModeTabs {

	const val CREATIVE_TAB_TRANSLATION_KEY = "itemGroup.critter_carts"

	val TABS_REGISTRY: DeferredRegister<CreativeModeTab> =
		DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, CritterCarts.MOD_ID)

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