package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.aaron.registry.AaronItemRegistry
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.item.CritterCageItem
import dev.aaronhowser.mods.critter_carts.item.ScoochwormSpawnEggItem
import dev.aaronhowser.mods.critter_carts.item.StorageAttachmentItem
import dev.aaronhowser.mods.critter_carts.entity.data.WormColor
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.Item
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister

object ModItems : AaronItemRegistry() {

	private const val DYEBERRY_DURATION = 20 * 60

	val ITEM_REGISTRY: DeferredRegister.Items = DeferredRegister.createItems(CritterCarts.MOD_ID)
	override fun getItemRegistry(): DeferredRegister.Items = ITEM_REGISTRY

	val SADDLEBAG: DeferredItem<StorageAttachmentItem> =
		register("saddlebag", StorageAttachmentItem::saddleBag, StorageAttachmentItem.DEFAULT_PROPERTIES)

	val WICKER_BASKET: DeferredItem<StorageAttachmentItem> =
		register("wicker_basket", StorageAttachmentItem::wickerBasket, StorageAttachmentItem.DEFAULT_PROPERTIES)

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

	val GREEN_DYEBERRY: DeferredItem<Item> = registerDyeberry(WormColor.GREEN)
	val BLUE_DYEBERRY: DeferredItem<Item> = registerDyeberry(WormColor.BLUE)
	val RED_DYEBERRY: DeferredItem<Item> = registerDyeberry(WormColor.RED)
	val YELLOW_DYEBERRY: DeferredItem<Item> = registerDyeberry(WormColor.YELLOW)
	val MAGENTA_DYEBERRY: DeferredItem<Item> = registerDyeberry(WormColor.MAGENTA)
	val CYAN_DYEBERRY: DeferredItem<Item> = registerDyeberry(WormColor.CYAN)

	private fun registerDyeberry(wormColor: WormColor): DeferredItem<Item> {
		return basic("${wormColor.colorName}_dyeberry") {
			val foodProperties = FoodProperties.Builder()
				.nutrition(2)
				.saturationModifier(0.1f)
				.alwaysEdible()
				.effect(
					{ MobEffectInstance(ModMobEffects.getDyedEffect(wormColor), DYEBERRY_DURATION) },
					1f
				)
				.build()

			Item.Properties().food(foodProperties)
		}
	}

	fun getDyeberry(wormColor: WormColor): DeferredItem<Item> {
		return when (wormColor) {
			WormColor.GREEN -> GREEN_DYEBERRY
			WormColor.BLUE -> BLUE_DYEBERRY
			WormColor.RED -> RED_DYEBERRY
			WormColor.YELLOW -> YELLOW_DYEBERRY
			WormColor.MAGENTA -> MAGENTA_DYEBERRY
			WormColor.CYAN -> CYAN_DYEBERRY
		}
	}
}