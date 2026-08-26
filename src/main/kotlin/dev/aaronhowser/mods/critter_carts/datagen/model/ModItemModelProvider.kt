package dev.aaronhowser.mods.critter_carts.datagen.model

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.registry.ModItems
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.PackOutput
import net.minecraft.world.item.Item
import net.neoforged.neoforge.client.model.generators.ItemModelProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper

class ModItemModelProvider(
	output: PackOutput,
	existingFileHelper: ExistingFileHelper
) : ItemModelProvider(output, CritterCarts.MOD_ID, existingFileHelper) {

	override fun registerModels() {
		basicItem(ModItems.SADDLEBAG.get())
		basicItem(ModItems.WICKER_BASKET.get())
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