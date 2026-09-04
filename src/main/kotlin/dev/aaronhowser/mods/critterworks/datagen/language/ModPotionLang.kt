package dev.aaronhowser.mods.critterworks.datagen.language

import dev.aaronhowser.mods.critterworks.registry.ModPotions
import net.minecraft.world.item.alchemy.Potion
import net.neoforged.neoforge.registries.DeferredHolder

object ModPotionLang {

	fun add(provider: ModLanguageProvider) {
		addDyeberryJuice(provider, ModPotions.DYED_GREEN, "Green")
		addDyeberryJuice(provider, ModPotions.DYED_BLUE, "Blue")
		addDyeberryJuice(provider, ModPotions.DYED_RED, "Red")
		addDyeberryJuice(provider, ModPotions.DYED_YELLOW, "Yellow")
		addDyeberryJuice(provider, ModPotions.DYED_MAGENTA, "Magenta")
		addDyeberryJuice(provider, ModPotions.DYED_CYAN, "Cyan")
		addAaronJuice(provider, ModPotions.AARON)
	}

	private fun addDyeberryJuice(
		provider: ModLanguageProvider,
		potion: DeferredHolder<Potion, Potion>,
		colorName: String
	) {
		val potionKey = potion.id.toLanguageKey()

		provider.add("item.minecraft.potion.effect.$potionKey", "$colorName Dyeberry Juice")
		provider.add("item.minecraft.splash_potion.effect.$potionKey", "Splash $colorName Dyeberry Juice")
		provider.add("item.minecraft.lingering_potion.effect.$potionKey", "Lingering $colorName Dyeberry Juice")
		provider.add("item.minecraft.tipped_arrow.effect.$potionKey", "Arrow of $colorName Dyeing")
	}

	private fun addAaronJuice(
		provider: ModLanguageProvider,
		potion: DeferredHolder<Potion, Potion>
	) {
		val potionKey = potion.id.toLanguageKey()

		provider.add("item.minecraft.potion.effect.$potionKey", "Aaron Juice")
		provider.add("item.minecraft.splash_potion.effect.$potionKey", "Splash Aaron Juice")
		provider.add("item.minecraft.lingering_potion.effect.$potionKey", "Lingering Aaron Juice")
		provider.add("item.minecraft.tipped_arrow.effect.$potionKey", "Arrow of Aaron")
	}
}