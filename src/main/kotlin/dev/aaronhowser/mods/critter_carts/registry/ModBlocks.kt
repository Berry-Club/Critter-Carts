package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.aaron.registry.AaronBlockRegistry
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.block.ColoredScoochstemBlock
import dev.aaronhowser.mods.critter_carts.block.ScoochstemBlock
import dev.aaronhowser.mods.critter_carts.entity.data.WormColor
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredRegister

object ModBlocks : AaronBlockRegistry() {

	val BLOCK_REGISTRY: DeferredRegister.Blocks = DeferredRegister.createBlocks(CritterCarts.MOD_ID)
	override fun getBlockRegistry(): DeferredRegister.Blocks = BLOCK_REGISTRY
	override fun getItemRegistry(): DeferredRegister.Items = ModItems.ITEM_REGISTRY

	val SCOOCHSTEM: DeferredBlock<ScoochstemBlock> =
		registerBlock("scoochstem", ::ScoochstemBlock)

	val SCOOCHSTEM_WOOD: DeferredBlock<ScoochstemBlock> =
		registerBlock("scoochstem_wood", ::ScoochstemBlock)

	val GREEN_SCOOCHSTEM: DeferredBlock<ColoredScoochstemBlock> =
		coloredScoochstem(WormColor.GREEN)

	val BLUE_SCOOCHSTEM: DeferredBlock<ColoredScoochstemBlock> =
		coloredScoochstem(WormColor.BLUE)

	val RED_SCOOCHSTEM: DeferredBlock<ColoredScoochstemBlock> =
		coloredScoochstem(WormColor.RED)

	val YELLOW_SCOOCHSTEM: DeferredBlock<ColoredScoochstemBlock> =
		coloredScoochstem(WormColor.YELLOW)

	val PURPLE_SCOOCHSTEM: DeferredBlock<ColoredScoochstemBlock> =
		coloredScoochstem(WormColor.PURPLE)

	val CYAN_SCOOCHSTEM: DeferredBlock<ColoredScoochstemBlock> =
		coloredScoochstem(WormColor.CYAN)

	private fun coloredScoochstem(wormColor: WormColor): DeferredBlock<ColoredScoochstemBlock> =
		registerBlock(wormColor.color + "_scoochstem") { ColoredScoochstemBlock(wormColor) }

}