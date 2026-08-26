package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.aaron.registry.AaronBlockRegistry
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.block.*
import dev.aaronhowser.mods.critter_carts.entity.data.WormColor
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.HugeMushroomBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredRegister

object ModBlocks : AaronBlockRegistry() {

	val BLOCK_REGISTRY: DeferredRegister.Blocks = DeferredRegister.createBlocks(CritterCarts.MOD_ID)
	override fun getBlockRegistry(): DeferredRegister.Blocks = BLOCK_REGISTRY
	override fun getItemRegistry(): DeferredRegister.Items = ModItems.ITEM_REGISTRY

	val SCOOCHSTEM: DeferredBlock<ScoochstemBlock> =
		registerBlock("scoochstem", ::ScoochstemBlock)

	val CRITTER_CAGE: DeferredBlock<CritterCageBlock> =
		registerBlockWithoutItem("critter_cage", ::CritterCageBlock)

	val SCOOCHSTEM_WOOD: DeferredBlock<ScoochstemBlock> =
		registerBlock("scoochstem_wood", ::ScoochstemBlock)

	val APPLE_SLICE: DeferredBlock<HugeMushroomBlock> =
		registerBlock("apple_slice") {
			HugeMushroomBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.RED_MUSHROOM_BLOCK))
		}

	val GREEN_SCOOCHSTEM: DeferredBlock<ColoredScoochstemBlock> =
		coloredScoochstem(WormColor.GREEN)

	val BLUE_SCOOCHSTEM: DeferredBlock<ColoredScoochstemBlock> =
		coloredScoochstem(WormColor.BLUE)

	val RED_SCOOCHSTEM: DeferredBlock<ColoredScoochstemBlock> =
		coloredScoochstem(WormColor.RED)

	val YELLOW_SCOOCHSTEM: DeferredBlock<ColoredScoochstemBlock> =
		coloredScoochstem(WormColor.YELLOW)

	val MAGENTA_SCOOCHSTEM: DeferredBlock<ColoredScoochstemBlock> =
		coloredScoochstem(WormColor.MAGENTA)

	val CYAN_SCOOCHSTEM: DeferredBlock<ColoredScoochstemBlock> =
		coloredScoochstem(WormColor.CYAN)

	val DYEBERRY_VINES: DeferredBlock<DyeberryVinesBlock> =
		registerBlockWithoutItem("dyeberry_vines", ::DyeberryVinesBlock)

	val DYEBERRY_VINES_PLANT: DeferredBlock<DyeberryVinesPlantBlock> =
		registerBlockWithoutItem("dyeberry_vines_plant", ::DyeberryVinesPlantBlock)

	private fun coloredScoochstem(wormColor: WormColor): DeferredBlock<ColoredScoochstemBlock> =
		registerBlock(wormColor.colorName + "_scoochstem") { ColoredScoochstemBlock(wormColor) }

}