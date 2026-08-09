package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.aaron.item.ItemWithTooltip
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.toComponent
import dev.aaronhowser.mods.aaron.registry.AaronItemRegistry
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.datagen.language.ModMenuLang
import dev.aaronhowser.mods.critter_carts.item.ScoochwormSpawnEggItem
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister

object ModItems : AaronItemRegistry() {

	val ITEM_REGISTRY: DeferredRegister.Items = DeferredRegister.createItems(CritterCarts.MOD_ID)
	override fun getItemRegistry(): DeferredRegister.Items = ITEM_REGISTRY

	val SADDLEBAG: DeferredItem<ItemWithTooltip> =
		register(
			"saddlebag",
			builder = { properties ->
				ItemWithTooltip(
					properties,
					ModMenuLang.SADDLE_NOT_INCLUDED.toComponent()
				)
			})

	val WICKER_BASKET: DeferredItem<ItemWithTooltip> =
		register(
			"wicker_basket",
			builder = { properties ->
				ItemWithTooltip(
					properties,
					ModMenuLang.NO_MENU_ACCESS.toComponent()
				)
			})

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