package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.aaron.registry.AaronItemRegistry
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.item.CritterCageItem
import dev.aaronhowser.mods.critter_carts.item.ScoochwormSpawnEggItem
import dev.aaronhowser.mods.critter_carts.item.StorageAttachmentItem
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister

object ModItems : AaronItemRegistry() {

	val ITEM_REGISTRY: DeferredRegister.Items = DeferredRegister.createItems(CritterCarts.MOD_ID)
	override fun getItemRegistry(): DeferredRegister.Items = ITEM_REGISTRY

	val SADDLEBAG: DeferredItem<StorageAttachmentItem> =
		register("saddlebag", StorageAttachmentItem::saddleBag, StorageAttachmentItem.DEFAULT_PROPERTIES)

	val WICKER_BASKET: DeferredItem<StorageAttachmentItem> =
		register("wicker_basket", StorageAttachmentItem::wickerBasket, StorageAttachmentItem.DEFAULT_PROPERTIES)

	val CRITTER_CAGE: DeferredItem<CritterCageItem> =
		register("critter_cage", ::CritterCageItem, PROPERTIES_SINGLE_STACK)

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