package dev.aaronhowser.mods.critter_carts.datagen.tag

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.add
import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.tags.EntityTypeTagsProvider
import net.minecraft.tags.EntityTypeTags
import java.util.concurrent.CompletableFuture

class ModEntityTypeTagsProvider(
	output: PackOutput,
	provider: CompletableFuture<HolderLookup.Provider>
) : EntityTypeTagsProvider(output, provider) {

	override fun addTags(provider: HolderLookup.Provider) {
		tag(EntityTypeTags.FALL_DAMAGE_IMMUNE)
			.add(
				ModEntityTypes.SCOOCHWORM,
				ModEntityTypes.SCOOCHWORM_PART
			)
	}

}