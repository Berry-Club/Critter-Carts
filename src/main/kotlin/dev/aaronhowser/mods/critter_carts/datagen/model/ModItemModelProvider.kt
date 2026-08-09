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
		spawnEggItem(ModItems.SCOOCHWORM_SPAWN_EGG.get())
	}
}