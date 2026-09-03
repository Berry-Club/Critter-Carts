package dev.aaronhowser.mods.critter_carts.datagen

import dev.aaronhowser.mods.aaron.datagen.AaronRecipeProvider
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.asIngredient
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.registry.ModBlocks
import dev.aaronhowser.mods.critter_carts.registry.ModItems
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredItem
import java.util.concurrent.CompletableFuture

class ModRecipeProvider(
	output: PackOutput,
	lookupProvider: CompletableFuture<HolderLookup.Provider>
) : AaronRecipeProvider(output, lookupProvider) {

	private fun modLoc(name: String) = CritterCarts.modResource(name)

	override fun buildRecipes(recipeOutput: RecipeOutput, holderLookup: HolderLookup.Provider) {
		buildShapedRecipes(recipeOutput, holderLookup)
		buildShapelessRecipes(recipeOutput, holderLookup)
		buildNamedRecipes(recipeOutput, holderLookup)
	}

	private fun buildShapelessRecipes(recipeOutput: RecipeOutput, holderLookup: HolderLookup.Provider) {
		fun coloredScoochstem(dyeBerry: DeferredItem<out Item>, output: DeferredBlock<out Block>) {
			shapelessRecipe(
				output,
				listOf(
					dyeBerry.asIngredient(),
					ModBlocks.SCOOCHSTEM.asIngredient()
				)
			).save(recipeOutput)
		}

		coloredScoochstem(ModItems.RED_DYEBERRY, ModBlocks.RED_SCOOCHSTEM)
		coloredScoochstem(ModItems.BLUE_DYEBERRY, ModBlocks.BLUE_SCOOCHSTEM)
		coloredScoochstem(ModItems.CYAN_DYEBERRY, ModBlocks.CYAN_SCOOCHSTEM)
		coloredScoochstem(ModItems.YELLOW_DYEBERRY, ModBlocks.YELLOW_SCOOCHSTEM)
		coloredScoochstem(ModItems.GREEN_DYEBERRY, ModBlocks.GREEN_SCOOCHSTEM)
		coloredScoochstem(ModItems.MAGENTA_DYEBERRY, ModBlocks.MAGENTA_SCOOCHSTEM)
	}

	private fun buildShapedRecipes(recipeOutput: RecipeOutput, holderLookup: HolderLookup.Provider) {
		shapedRecipe(
			ModBlocks.SCOOCHSTEM_WOOD.toStack(4),
			"WW,WW",
			mapOf(
				'W' to ModBlocks.SCOOCHSTEM.asIngredient()
			)
		).save(recipeOutput)
	}

	private fun buildNamedRecipes(recipeOutput: RecipeOutput, holderLookup: HolderLookup.Provider) {
		shapelessRecipe(
			Items.RED_DYE,
			listOf(ModItems.RED_DYEBERRY.asIngredient())
		).save(recipeOutput, modLoc("red_dye_from_dyeberry"))

		shapelessRecipe(
			Items.BLUE_DYE,
			listOf(ModItems.BLUE_DYEBERRY.asIngredient())
		).save(recipeOutput, modLoc("blue_dye_from_dyeberry"))

		shapelessRecipe(
			Items.GREEN_DYE,
			listOf(ModItems.GREEN_DYEBERRY.asIngredient())
		).save(recipeOutput, modLoc("green_dye_from_dyeberry"))

		shapelessRecipe(
			Items.CYAN_DYE,
			listOf(ModItems.CYAN_DYEBERRY.asIngredient())
		).save(recipeOutput, modLoc("cyan_dye_from_dyeberry"))

		shapelessRecipe(
			Items.YELLOW_DYE,
			listOf(ModItems.YELLOW_DYEBERRY.asIngredient())
		).save(recipeOutput, modLoc("yellow_dye_from_dyeberry"))

		shapelessRecipe(
			Items.MAGENTA_DYE,
			listOf(ModItems.MAGENTA_DYEBERRY.asIngredient())
		).save(recipeOutput, modLoc("magenta_dye_from_dyeberry"))

		shapelessRecipe(
			Items.ROTTEN_FLESH,
			listOf(ModItems.AARONBERRY.asIngredient())
		).save(recipeOutput, modLoc("rotten_flesh_from_aaronberry"))

		shapelessRecipe(
			Items.APPLE,
			9,
			listOf(ModBlocks.APPLE_SLICE.asIngredient())
		).save(recipeOutput, modLoc("apples_from_apple_slice"))
	}

}