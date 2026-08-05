package dev.aaronhowser.mods.critter_carts.registry

import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister

object ModRegistries {

	fun register(modBus: IEventBus) {
		val registries: List<DeferredRegister<*>> = listOf(
			ModItems.ITEM_REGISTRY,
			ModBlocks.BLOCK_REGISTRY,
			ModCreativeModeTabs.TABS_REGISTRY,
			ModEntityTypes.ENTITY_TYPE_REGISTRY,
			ModEntityDataSerializers.ENTITY_DATA_SERIALIZER_REGISTRY,
			ModSoundEvents.SOUND_EVENT_REGISTRY
		)

		for (registry in registries) {
			registry.register(modBus)
		}
	}
}