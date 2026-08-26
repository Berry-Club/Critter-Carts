package dev.aaronhowser.mods.critter_carts.datagen.tag

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.registry.ModMobEffectTags
import dev.aaronhowser.mods.critter_carts.registry.ModMobEffects
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.PackOutput
import net.minecraft.data.tags.TagsProvider
import net.minecraft.world.effect.MobEffect
import net.neoforged.neoforge.common.data.ExistingFileHelper
import java.util.concurrent.CompletableFuture

class ModMobEffectTagsProvider(
	output: PackOutput,
	lookupProvider: CompletableFuture<HolderLookup.Provider>,
	existingFileHelper: ExistingFileHelper
) : TagsProvider<MobEffect>(
	output,
	Registries.MOB_EFFECT,
	lookupProvider,
	CritterCarts.MOD_ID,
	existingFileHelper
) {

	override fun addTags(provider: HolderLookup.Provider) {
		tag(ModMobEffectTags.DYES_ENTITY)
			.add(
				ModMobEffects.DYED_GREEN.key,
				ModMobEffects.DYED_BLUE.key,
				ModMobEffects.DYED_RED.key,
				ModMobEffects.DYED_YELLOW.key,
				ModMobEffects.DYED_MAGENTA.key,
				ModMobEffects.DYED_CYAN.key,
				ModMobEffects.AARON.key
			)
	}
}