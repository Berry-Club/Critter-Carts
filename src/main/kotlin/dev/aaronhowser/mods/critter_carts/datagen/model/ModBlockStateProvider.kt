package dev.aaronhowser.mods.critter_carts.datagen.model

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.registry.ModBlocks
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.client.model.generators.BlockStateProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper

class ModBlockStateProvider(
	output: PackOutput,
	existingFileHelper: ExistingFileHelper
) : BlockStateProvider(output, CritterCarts.MOD_ID, existingFileHelper) {

	override fun registerStatesAndModels() {
		val scoochstem = ModBlocks.SCOOCHSTEM.get()
		val oakLog = ResourceLocation.withDefaultNamespace("block/oak_log")
		val oakLogTop = ResourceLocation.withDefaultNamespace("block/oak_log_top")

		axisBlock(scoochstem, oakLog, oakLogTop)
		simpleBlockItem(scoochstem, models().getExistingFile(CritterCarts.modResource("block/scoochstem")))
	}
}