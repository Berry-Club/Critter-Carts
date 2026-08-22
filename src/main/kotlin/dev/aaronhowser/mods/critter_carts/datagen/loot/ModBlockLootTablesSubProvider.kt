package dev.aaronhowser.mods.critter_carts.datagen.loot

import dev.aaronhowser.mods.critter_carts.registry.ModBlocks
import net.minecraft.core.HolderLookup
import net.minecraft.data.loot.BlockLootSubProvider
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.registries.DeferredHolder

class ModBlockLootTablesSubProvider(
	provider: HolderLookup.Provider
) : BlockLootSubProvider(setOf(), FeatureFlags.REGISTRY.allFlags(), provider) {

	override fun generate() {
		dropSelf(ModBlocks.SCOOCHSTEM.get())
		dropSelf(ModBlocks.SCOOCHSTEM_WOOD.get())
	}

	override fun getKnownBlocks(): Iterable<Block> {
		return ModBlocks.BLOCK_REGISTRY.entries.map(DeferredHolder<Block, out Block>::get)
	}
}