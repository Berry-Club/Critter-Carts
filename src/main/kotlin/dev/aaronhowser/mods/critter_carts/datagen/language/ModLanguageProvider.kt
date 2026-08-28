package dev.aaronhowser.mods.critter_carts.datagen.language

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.registry.ModBlocks
import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes
import dev.aaronhowser.mods.critter_carts.registry.ModMobEffects
import dev.aaronhowser.mods.critter_carts.registry.ModSoundEvents
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.LanguageProvider

class ModLanguageProvider(
	output: PackOutput
) : LanguageProvider(output, CritterCarts.MOD_ID, "en_us") {

	override fun addTranslations() {
		ModAdvancementLang.add(this)
		ModItemLang.add(this)
		ModMenuLang.add(this)

		addBlock(ModBlocks.SCOOCHSTEM, "Scoochstem")
		addBlock(ModBlocks.SCOOCHSTEM_WOOD, "Scoochstem Wood")
		addBlock(ModBlocks.APPLE_SLICE, "Apple Slice")

		addBlock(ModBlocks.GREEN_SCOOCHSTEM, "Green Scoochstem")
		addBlock(ModBlocks.BLUE_SCOOCHSTEM, "Blue Scoochstem")
		addBlock(ModBlocks.RED_SCOOCHSTEM, "Red Scoochstem")
		addBlock(ModBlocks.YELLOW_SCOOCHSTEM, "Yellow Scoochstem")
		addBlock(ModBlocks.MAGENTA_SCOOCHSTEM, "Magenta Scoochstem")
		addBlock(ModBlocks.CYAN_SCOOCHSTEM, "Cyan Scoochstem")

		addEntityType(ModEntityTypes.SCOOCHWORM, "Scoochworm")

		addEffect(ModMobEffects.DYED_GREEN, "Dyed Green")
		addEffect(ModMobEffects.DYED_BLUE, "Dyed Blue")
		addEffect(ModMobEffects.DYED_RED, "Dyed Red")
		addEffect(ModMobEffects.DYED_YELLOW, "Dyed Yellow")
		addEffect(ModMobEffects.DYED_MAGENTA, "Dyed Magenta")
		addEffect(ModMobEffects.DYED_CYAN, "Dyed Cyan")
		addEffect(ModMobEffects.AARON, "Aaron.")

		addPotion("dyed_green", "Green")
		addPotion("dyed_blue", "Blue")
		addPotion("dyed_red", "Red")
		addPotion("dyed_yellow", "Yellow")
		addPotion("dyed_magenta", "Magenta")
		addPotion("dyed_cyan", "Cyan")

		add(ModSoundEvents.SCOOCHWORM_FOOTSTEP_SUBTITLE, "Scoochworm scooches")
	}

	private fun addPotion(potionName: String, colorName: String) {
		val potionKey = "${CritterCarts.MOD_ID}.$potionName"

		add("item.minecraft.potion.effect.$potionKey", "Potion of $colorName Dyeing")
		add("item.minecraft.splash_potion.effect.$potionKey", "Splash Potion of $colorName Dyeing")
		add("item.minecraft.lingering_potion.effect.$potionKey", "Lingering Potion of $colorName Dyeing")
		add("item.minecraft.tipped_arrow.effect.$potionKey", "Arrow of $colorName Dyeing")
	}
}