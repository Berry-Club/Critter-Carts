package dev.aaronhowser.mods.critter_carts.datagen.language

import dev.aaronhowser.mods.critter_carts.CritterCarts
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.LanguageProvider

class ModLanguageProvider(
	output: PackOutput
) : LanguageProvider(output, CritterCarts.MOD_ID, "en_us") {

	override fun addTranslations() {
		ModAdvancementLang.add(this)
		ModBlockLang.add(this)
		ModEffectLang.add(this)
		ModEntityLang.add(this)
		ModItemLang.add(this)
		ModMenuLang.add(this)
		ModPotionLang.add(this)
		ModSoundLang.add(this)
	}
}