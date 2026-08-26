package dev.aaronhowser.mods.critter_carts.datagen.model

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.registry.ModItems
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.client.model.generators.ItemModelProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper

class ModItemModelProvider(
	output: PackOutput,
	existingFileHelper: ExistingFileHelper
) : ItemModelProvider(output, CritterCarts.MOD_ID, existingFileHelper) {

	override fun registerModels() {
		basicItem(ModItems.SADDLEBAG.get())
		basicItem(ModItems.WICKER_BASKET.get())
		basicItem(ModItems.GREEN_DYEBERRY.get())
		basicItem(ModItems.BLUE_DYEBERRY.get())
		basicItem(ModItems.RED_DYEBERRY.get())
		basicItem(ModItems.YELLOW_DYEBERRY.get())
		basicItem(ModItems.MAGENTA_DYEBERRY.get())
		basicItem(ModItems.CYAN_DYEBERRY.get())
		spawnEggItem(ModItems.SCOOCHWORM_SPAWN_EGG.get())
	}
}