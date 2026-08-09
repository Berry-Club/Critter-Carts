package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.aaron.registry.AaronItemRegistry
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.item.SaddlebagItem
import dev.aaronhowser.mods.critter_carts.item.ScoochwormSpawnEggItem
import dev.aaronhowser.mods.critter_carts.item.WickerBasketItem
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister

object ModItems : AaronItemRegistry() {

	val ITEM_REGISTRY: DeferredRegister.Items = DeferredRegister.createItems(CritterCarts.MOD_ID)
	override fun getItemRegistry(): DeferredRegister.Items = ITEM_REGISTRY

	val SADDLEBAG: DeferredItem<SaddlebagItem> =
		register(
			"saddlebag",
			builder = { properties ->
				SaddlebagItem(properties)
			})

	val WICKER_BASKET: DeferredItem<WickerBasketItem> =
		register("wicker_basket", ::WickerBasketItem, WickerBasketItem.DEFAULT_PROPERTIES)

	val SCOOCHWORM_SPAWN_EGG: DeferredItem<ScoochwormSpawnEggItem> =
		register(
			"scoochworm_spawn_egg",
			builder = { properties ->
				ScoochwormSpawnEggItem(
					ModEntityTypes.SCOOCHWORM,
					0x95E4ED, 0x6DCF72,
					properties
				)
			})
}