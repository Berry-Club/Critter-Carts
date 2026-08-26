package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.critter_carts.CritterCarts
import net.minecraft.core.registries.Registries
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.Potions
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object ModPotions {

	private const val POTION_DURATION = 20 * 60 * 5

	val POTION_REGISTRY: DeferredRegister<Potion> =
		DeferredRegister.create(Registries.POTION, CritterCarts.MOD_ID)

	val DYED_GREEN: DeferredHolder<Potion, Potion> =
		register("dyed_green") { MobEffectInstance(ModMobEffects.DYED_GREEN, POTION_DURATION) }
	val DYED_BLUE: DeferredHolder<Potion, Potion> =
		register("dyed_blue") { MobEffectInstance(ModMobEffects.DYED_BLUE, POTION_DURATION) }
	val DYED_RED: DeferredHolder<Potion, Potion> =
		register("dyed_red") { MobEffectInstance(ModMobEffects.DYED_RED, POTION_DURATION) }
	val DYED_YELLOW: DeferredHolder<Potion, Potion> =
		register("dyed_yellow") { MobEffectInstance(ModMobEffects.DYED_YELLOW, POTION_DURATION) }
	val DYED_MAGENTA: DeferredHolder<Potion, Potion> =
		register("dyed_magenta") { MobEffectInstance(ModMobEffects.DYED_MAGENTA, POTION_DURATION) }
	val DYED_CYAN: DeferredHolder<Potion, Potion> =
		register("dyed_cyan") { MobEffectInstance(ModMobEffects.DYED_CYAN, POTION_DURATION) }

	private fun register(
		name: String,
		effect: () -> MobEffectInstance
	): DeferredHolder<Potion, Potion> {
		return POTION_REGISTRY.register(name, Supplier {
			Potion(CritterCarts.MOD_ID + "." + name, effect())
		})
	}

	fun registerRecipes(event: RegisterBrewingRecipesEvent) {
		val builder = event.builder

		builder.addMix(Potions.AWKWARD, ModItems.GREEN_DYEBERRY.get(), DYED_GREEN)
		builder.addMix(Potions.AWKWARD, ModItems.BLUE_DYEBERRY.get(), DYED_BLUE)
		builder.addMix(Potions.AWKWARD, ModItems.RED_DYEBERRY.get(), DYED_RED)
		builder.addMix(Potions.AWKWARD, ModItems.YELLOW_DYEBERRY.get(), DYED_YELLOW)
		builder.addMix(Potions.AWKWARD, ModItems.MAGENTA_DYEBERRY.get(), DYED_MAGENTA)
		builder.addMix(Potions.AWKWARD, ModItems.CYAN_DYEBERRY.get(), DYED_CYAN)
	}
}