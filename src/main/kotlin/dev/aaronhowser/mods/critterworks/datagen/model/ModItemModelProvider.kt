package dev.aaronhowser.mods.critterworks.datagen.model

import dev.aaronhowser.mods.critterworks.Critterworks
import dev.aaronhowser.mods.critterworks.registry.ModItems
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.PackOutput
import net.minecraft.world.item.Item
import net.neoforged.neoforge.client.model.generators.ItemModelProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper

class ModItemModelProvider(
	output: PackOutput,
	existingFileHelper: ExistingFileHelper
) : ItemModelProvider(output, Critterworks.MOD_ID, existingFileHelper) {

	override fun registerModels() {
		basicItem(ModItems.LOCKBOX.get())
		basicItem(ModItems.WEB_FLUID.get())
		basicItem(ModItems.WEB_PATHFINDER.get())
		basicItem(ModItems.ITEM_FILTER.get())
		withExistingParent("spider_nest_interface", mcLoc("item/generated"))
			.texture("layer0", modLoc("item/web_pathfinder"))
		dyeberryItem(ModItems.GREEN_DYEBERRY.get(), "green")
		dyeberryItem(ModItems.BLUE_DYEBERRY.get(), "blue")
		dyeberryItem(ModItems.RED_DYEBERRY.get(), "red")
		dyeberryItem(ModItems.YELLOW_DYEBERRY.get(), "yellow")
		dyeberryItem(ModItems.MAGENTA_DYEBERRY.get(), "magenta")
		dyeberryItem(ModItems.CYAN_DYEBERRY.get(), "cyan")
		dyeberryItem(ModItems.AARONBERRY.get(), "aaron")
		spawnEggItem(ModItems.SCOOCHWORM_SPAWN_EGG.get())
	}

	private fun dyeberryItem(item: Item, textureName: String) {
		val itemName = BuiltInRegistries.ITEM.getKey(item).path

		withExistingParent(itemName, mcLoc("item/generated"))
			.texture("layer0", modLoc("item/dyeberry/$textureName"))
	}
}