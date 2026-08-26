package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.entity.data.WormColor
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.Potions
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object ModPotions {

	private const val POTION_DURATION = 20 * 60 * 5

	val POTION_REGISTRY: DeferredRegister<Potion> =
		DeferredRegister.create(Registries.POTION, CritterCarts.MOD_ID)

	val DYED_GREEN: Holder<Potion> = registerDyed(WormColor.GREEN)
	val DYED_BLUE: Holder<Potion> = registerDyed(WormColor.BLUE)
	val DYED_RED: Holder<Potion> = registerDyed(WormColor.RED)
	val DYED_YELLOW: Holder<Potion> = registerDyed(WormColor.YELLOW)
	val DYED_MAGENTA: Holder<Potion> = registerDyed(WormColor.MAGENTA)
	val DYED_CYAN: Holder<Potion> = registerDyed(WormColor.CYAN)

	private fun registerDyed(wormColor: WormColor): Holder<Potion> {
		val name = "dyed_${wormColor.colorName}"

		return POTION_REGISTRY.register(name, Supplier {
			Potion(
				"${CritterCarts.MOD_ID}.$name",
				MobEffectInstance(ModMobEffects.getDyedEffect(wormColor), POTION_DURATION)
			)
		})
	}

	fun registerRecipes(event: RegisterBrewingRecipesEvent) {
		for (wormColor in WormColor.entries) {
			event.builder.addMix(
				Potions.AWKWARD,
				ModItems.getDyeberry(wormColor).get(),
				getDyedPotion(wormColor)
			)
		}
	}

	fun getDyedPotion(wormColor: WormColor): Holder<Potion> {
		return when (wormColor) {
			WormColor.GREEN -> DYED_GREEN
			WormColor.BLUE -> DYED_BLUE
			WormColor.RED -> DYED_RED
			WormColor.YELLOW -> DYED_YELLOW
			WormColor.MAGENTA -> DYED_MAGENTA
			WormColor.CYAN -> DYED_CYAN
		}
	}
}