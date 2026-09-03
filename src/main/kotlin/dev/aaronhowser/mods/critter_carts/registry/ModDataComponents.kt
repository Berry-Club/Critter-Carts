package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.aaron.registry.AaronDataComponentRegistry
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.entity.data.WormColor
import dev.aaronhowser.mods.critter_carts.item.component.ItemFilterComponent
import dev.aaronhowser.mods.critter_carts.item.component.NestInterfaceComponent
import dev.aaronhowser.mods.critter_carts.item.component.WebNodeDataComponent
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.component.CustomData
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

object ModDataComponents : AaronDataComponentRegistry() {

	val DATA_COMPONENT_REGISTRY: DeferredRegister.DataComponents =
		DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CritterCarts.MOD_ID)

	override fun getDataComponentRegistry(): DeferredRegister.DataComponents = DATA_COMPONENT_REGISTRY

	val ENTITY_DATA: DeferredHolder<DataComponentType<*>, DataComponentType<CustomData>> =
		register("entity_data", CustomData.CODEC, CustomData.STREAM_CODEC)

	val WORM_COLOR: DeferredHolder<DataComponentType<*>, DataComponentType<WormColor>> =
		register("worm_color", WormColor.CODEC, WormColor.STREAM_CODEC)

	val WEB_NODE: DeferredHolder<DataComponentType<*>, DataComponentType<WebNodeDataComponent>> =
		register("web_node", WebNodeDataComponent.CODEC, WebNodeDataComponent.STREAM_CODEC)

	val ITEM_FILTER: DeferredHolder<DataComponentType<*>, DataComponentType<ItemFilterComponent>> =
		register("item_filter", ItemFilterComponent.CODEC, ItemFilterComponent.STREAM_CODEC)

	val NEST_INTERFACE: DeferredHolder<DataComponentType<*>, DataComponentType<NestInterfaceComponent>> =
		register("nest_interface", NestInterfaceComponent.CODEC, NestInterfaceComponent.STREAM_CODEC)
}