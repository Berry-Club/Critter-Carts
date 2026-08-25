package dev.aaronhowser.mods.critter_carts.datagen.loot

import dev.aaronhowser.mods.critter_carts.registry.ModBlocks
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.loot.BlockLootSubProvider
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator
import net.neoforged.neoforge.registries.DeferredHolder

class ModBlockLootTablesSubProvider(
	private val provider: HolderLookup.Provider
) : BlockLootSubProvider(setOf(), FeatureFlags.REGISTRY.allFlags(), provider) {

	override fun generate() {
		dropSelf(ModBlocks.SCOOCHSTEM.get())
		dropSelf(ModBlocks.SCOOCHSTEM_WOOD.get())
		appleSlice()

		dropSelf(ModBlocks.GREEN_SCOOCHSTEM.get())
		dropSelf(ModBlocks.BLUE_SCOOCHSTEM.get())
		dropSelf(ModBlocks.RED_SCOOCHSTEM.get())
		dropSelf(ModBlocks.YELLOW_SCOOCHSTEM.get())
		dropSelf(ModBlocks.MAGENTA_SCOOCHSTEM.get())
		dropSelf(ModBlocks.CYAN_SCOOCHSTEM.get())
	}

	private fun appleSlice() {
		val fortune = provider
			.lookupOrThrow(Registries.ENCHANTMENT)
			.getOrThrow(Enchantments.FORTUNE)
		val appleDrops = LootItem.lootTableItem(Items.APPLE)
			.apply(SetItemCountFunction.setCount(UniformGenerator.between(1f, 3f)))
			.apply(ApplyBonusCount.addUniformBonusCount(fortune))

		add(
			ModBlocks.APPLE_SLICE.get(),
			createSilkTouchDispatchTable(ModBlocks.APPLE_SLICE.get(), appleDrops)
		)
	}

	override fun getKnownBlocks(): Iterable<Block> {
		return ModBlocks.BLOCK_REGISTRY.entries.map(DeferredHolder<Block, out Block>::get)
	}
}