package dev.aaronhowser.mods.critter_carts.datagen.language

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.registry.ModBlocks
import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes
import dev.aaronhowser.mods.critter_carts.registry.ModSoundEvents
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.LanguageProvider

class ModLanguageProvider(
	output: PackOutput
) : LanguageProvider(output, CritterCarts.MOD_ID, "en_us") {

	override fun addTranslations() {
		ModItemLang.add(this)
		ModMenuLang.add(this)

		addBlock(ModBlocks.SCOOCHSTEM, "Scoochstem")
		addEntityType(ModEntityTypes.SCOOCHWORM, "Scoochworm")
		add(ModSoundEvents.SCOOCHWORM_FOOTSTEP_SUBTITLE, "Scoochworm scooches")
	}
}