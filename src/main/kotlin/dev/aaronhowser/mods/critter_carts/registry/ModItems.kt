package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.aaron.registry.AaronItemRegistry
import dev.aaronhowser.mods.critter_carts.CritterCarts
import net.minecraft.world.item.SpawnEggItem
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister

object ModItems : AaronItemRegistry() {

	val ITEM_REGISTRY: DeferredRegister.Items = DeferredRegister.createItems(CritterCarts.MOD_ID)
	override fun getItemRegistry(): DeferredRegister.Items = ITEM_REGISTRY

	val SCOOCHWORM_SPAWN_EGG: DeferredItem<SpawnEggItem> =
		registerSpawnEgg("scoochworm_spawn_egg", ModEntityTypes.SCOOCHWORM::get, 0x95E4ED, 0x6DCF72)
}