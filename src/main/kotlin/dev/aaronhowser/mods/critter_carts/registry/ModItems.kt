package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.aaron.registry.AaronItemRegistry
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.entity.data.WormColor
import dev.aaronhowser.mods.critter_carts.item.CritterCageItem
import dev.aaronhowser.mods.critter_carts.item.DyeberryItem
import dev.aaronhowser.mods.critter_carts.item.ScoochwormSpawnEggItem
import dev.aaronhowser.mods.critter_carts.item.StorageAttachmentItem
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister

object ModItems : AaronItemRegistry() {

	val ITEM_REGISTRY: DeferredRegister.Items = DeferredRegister.createItems(CritterCarts.MOD_ID)
	override fun getItemRegistry(): DeferredRegister.Items = ITEM_REGISTRY

	val LOCKBOX: DeferredItem<StorageAttachmentItem> =
		register("lockbox", ::StorageAttachmentItem, StorageAttachmentItem.DEFAULT_PROPERTIES)

	val CRITTER_CAGE: DeferredItem<CritterCageItem> =
		register(
			"critter_cage",
			{ properties -> CritterCageItem(ModBlocks.CRITTER_CAGE.get(), properties) },
			PROPERTIES_SINGLE_STACK
		)

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

	val GREEN_DYEBERRY: DeferredItem<DyeberryItem> =
		registerDyeberry(WormColor.GREEN)
	val BLUE_DYEBERRY: DeferredItem<DyeberryItem> =
		registerDyeberry(WormColor.BLUE)
	val RED_DYEBERRY: DeferredItem<DyeberryItem> =
		registerDyeberry(WormColor.RED)
	val YELLOW_DYEBERRY: DeferredItem<DyeberryItem> =
		registerDyeberry(WormColor.YELLOW)
	val MAGENTA_DYEBERRY: DeferredItem<DyeberryItem> =
		registerDyeberry(WormColor.MAGENTA)
	val CYAN_DYEBERRY: DeferredItem<DyeberryItem> =
		registerDyeberry(WormColor.CYAN)
	val AARONBERRY: DeferredItem<DyeberryItem> =
		registerDyeberry(WormColor.AARON, "aaronberry")

	private fun registerDyeberry(
		wormColor: WormColor,
		name: String = wormColor.colorName + "_dyeberry"
	): DeferredItem<DyeberryItem> {
		return register(
			name,
			{ properties ->
				DyeberryItem(
					ModBlocks.DYEBERRY_VINES.get(),
					wormColor,
					properties
				)
			}
		) { DyeberryItem.getProperties(wormColor) }
	}

}