package dev.aaronhowser.mods.critter_carts.datagen.tag

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.registry.ModBlocks
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.tags.BlockTags
import net.neoforged.neoforge.common.data.BlockTagsProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper
import java.util.concurrent.CompletableFuture

class ModBlockTagsProvider(
	output: PackOutput,
	lookupProvider: CompletableFuture<HolderLookup.Provider>,
	existingFileHelper: ExistingFileHelper
) : BlockTagsProvider(output, lookupProvider, CritterCarts.MOD_ID, existingFileHelper) {

	override fun addTags(provider: HolderLookup.Provider) {
		tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.SCOOCHSTEM.get())
		tag(BlockTags.LOGS).add(ModBlocks.SCOOCHSTEM.get())
		tag(BlockTags.LOGS_THAT_BURN).add(ModBlocks.SCOOCHSTEM.get())
	}
}