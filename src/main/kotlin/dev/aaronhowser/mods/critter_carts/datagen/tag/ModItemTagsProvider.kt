package dev.aaronhowser.mods.critter_carts.datagen.tag

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.add
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.registry.ModBlocks
import dev.aaronhowser.mods.critter_carts.registry.ModItems
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.tags.ItemTagsProvider
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.common.Tags
import net.neoforged.neoforge.common.data.ExistingFileHelper
import java.util.concurrent.CompletableFuture

class ModItemTagsProvider(
	output: PackOutput,
	lookupProvider: CompletableFuture<HolderLookup.Provider>,
	blockTags: CompletableFuture<TagLookup<Block>>,
	existingFileHelper: ExistingFileHelper
) : ItemTagsProvider(output, lookupProvider, blockTags, CritterCarts.MOD_ID, existingFileHelper) {

	override fun addTags(provider: HolderLookup.Provider) {
		tag(DYEBERRIES)
			.add(
				ModItems.RED_DYEBERRY,
				ModItems.BLUE_DYEBERRY,
				ModItems.GREEN_DYEBERRY,
				ModItems.CYAN_DYEBERRY,
				ModItems.YELLOW_DYEBERRY,
				ModItems.MAGENTA_DYEBERRY,
			)

		tag(SCOOCHWORM_LOOK_AT)
			.add(Items.MELON)
			.addTag(DYEBERRIES)

		tag(SCOOCHWORM_SADDLES)
			.add(Items.SADDLE)

		tag(WEB_LINE_INTERACTABLE)
			.add(ModItems.WEB_FLUID)
			.add(ModItems.WEB_PATHFINDER)
			.addTag(Tags.Items.TOOLS_SHEAR)

		tag(ItemTags.LOGS)
			.add(
				ModBlocks.SCOOCHSTEM.asItem(),
				ModBlocks.SCOOCHSTEM_WOOD.asItem()
			)

		tag(ItemTags.LOGS_THAT_BURN)
			.add(
				ModBlocks.SCOOCHSTEM.asItem(),
				ModBlocks.SCOOCHSTEM_WOOD.asItem()
			)
	}

	companion object {
		val DYEBERRIES = create("dyeberries")
		val SCOOCHWORM_LOOK_AT = create("scoochworm_look_at")
		val SCOOCHWORM_SADDLES = create("scoochworm_saddles")
		val WEB_LINE_INTERACTABLE = create("web_line_interactable")

		private fun create(id: String): TagKey<Item> = ItemTags.create(CritterCarts.modResource(id))
	}
}