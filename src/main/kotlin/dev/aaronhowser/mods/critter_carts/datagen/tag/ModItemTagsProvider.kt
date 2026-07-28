package dev.aaronhowser.mods.critter_carts.datagen.tag

import dev.aaronhowser.mods.critter_carts.CritterCarts
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.tags.ItemTagsProvider
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.common.data.ExistingFileHelper
import java.util.concurrent.CompletableFuture

class ModItemTagsProvider(
	output: PackOutput,
	lookupProvider: CompletableFuture<HolderLookup.Provider>,
	blockTags: CompletableFuture<TagLookup<Block>>,
	existingFileHelper: ExistingFileHelper
) : ItemTagsProvider(output, lookupProvider, blockTags, CritterCarts.MOD_ID, existingFileHelper) {

	override fun addTags(provider: HolderLookup.Provider) {
		tag(SCOOCHWORM_CHEST_ATTACHMENTS).add(Items.CHEST)
		tag(SCOOCHWORM_SADDLE_ATTACHMENTS).add(Items.SADDLE)
	}

	companion object {
		val SCOOCHWORM_CHEST_ATTACHMENTS = create("scoochworm_chests")
		val SCOOCHWORM_SADDLE_ATTACHMENTS = create("scoochworm_saddles")

		private fun create(id: String): TagKey<Item> = ItemTags.create(CritterCarts.modResource(id))
	}
}