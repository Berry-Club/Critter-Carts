package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.item.DyeberryItem
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.Potions
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object ModPotions {

	val POTION_REGISTRY: DeferredRegister<Potion> =
		DeferredRegister.create(Registries.POTION, CritterCarts.MOD_ID)

	val DYED_GREEN: DeferredHolder<Potion, Potion> =
		register("dyed_green") { createEffect(ModMobEffects.DYED_GREEN) }
	val DYED_BLUE: DeferredHolder<Potion, Potion> =
		register("dyed_blue") { createEffect(ModMobEffects.DYED_BLUE) }
	val DYED_RED: DeferredHolder<Potion, Potion> =
		register("dyed_red") { createEffect(ModMobEffects.DYED_RED) }
	val DYED_YELLOW: DeferredHolder<Potion, Potion> =
		register("dyed_yellow") { createEffect(ModMobEffects.DYED_YELLOW) }
	val DYED_MAGENTA: DeferredHolder<Potion, Potion> =
		register("dyed_magenta") { createEffect(ModMobEffects.DYED_MAGENTA) }
	val DYED_CYAN: DeferredHolder<Potion, Potion> =
		register("dyed_cyan") { createEffect(ModMobEffects.DYED_CYAN) }
	val AARON: DeferredHolder<Potion, Potion> =
		register("aaron") { createEffect(ModMobEffects.AARON) }

	private fun createEffect(effect: Holder<MobEffect>): MobEffectInstance {
		return MobEffectInstance(
			effect,
			DyeberryItem.POTION_DURATION,
			0,
			false,
			true,
			true
		)
	}

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
		builder.addMix(Potions.AWKWARD, ModItems.AARONBERRY.get(), AARON)
	}
}