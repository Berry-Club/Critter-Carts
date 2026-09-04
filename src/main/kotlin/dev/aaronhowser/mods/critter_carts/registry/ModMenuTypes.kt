package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.aaron.registry.AaronMenuTypesRegistry
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.menu.item_filter.ItemFilterMenu
import dev.aaronhowser.mods.critter_carts.menu.item_filter.ItemFilterScreen
import dev.aaronhowser.mods.critter_carts.menu.nest_interface.NestInterfaceMenu
import dev.aaronhowser.mods.critter_carts.menu.nest_interface.NestInterfaceScreen
import dev.aaronhowser.mods.critter_carts.menu.spider_nest.SpiderNestMenu
import dev.aaronhowser.mods.critter_carts.menu.spider_nest.SpiderNestScreen
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.inventory.MenuType
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

object ModMenuTypes : AaronMenuTypesRegistry() {

	val MENU_TYPE_REGISTRY: DeferredRegister<MenuType<*>> =
		DeferredRegister.create(BuiltInRegistries.MENU, CritterCarts.MOD_ID)

	override fun getMenuTypeRegistry(): DeferredRegister<MenuType<*>> = MENU_TYPE_REGISTRY

	val ITEM_FILTER: DeferredHolder<MenuType<*>, MenuType<ItemFilterMenu>> =
		register("item_filter") { IMenuTypeExtension.create(::ItemFilterMenu) }

	val NEST_INTERFACE: DeferredHolder<MenuType<*>, MenuType<NestInterfaceMenu>> =
		register("nest_interface") { IMenuTypeExtension.create(NestInterfaceMenu::fromNetwork) }

	val SPIDER_NEST: DeferredHolder<MenuType<*>, MenuType<SpiderNestMenu>> =
		register("spider_nest") { IMenuTypeExtension.create(SpiderNestMenu::fromNetwork) }

	override fun registerScreens(event: RegisterMenuScreensEvent) {
		event.register(ITEM_FILTER.get(), ::ItemFilterScreen)
		event.register(NEST_INTERFACE.get(), ::NestInterfaceScreen)
		event.register(SPIDER_NEST.get(), ::SpiderNestScreen)
	}
}