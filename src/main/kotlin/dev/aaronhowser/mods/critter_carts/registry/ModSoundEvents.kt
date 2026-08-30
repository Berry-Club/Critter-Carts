package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.critter_carts.CritterCarts
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.sounds.SoundEvent
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object ModSoundEvents {
	const val SCOOCHWORM_FOOTSTEP_SUBTITLE = "subtitles.critter_carts.scoochworm_footstep"
	const val SCOOCHWORM_KISS_SUBTITLE = "subtitles.critter_carts.scoochworm_kiss"

	val SOUND_EVENT_REGISTRY: DeferredRegister<SoundEvent> =
		DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, CritterCarts.MOD_ID)

	val SCOOCHWORM_FOOTSTEP: DeferredHolder<SoundEvent, SoundEvent> =
		register("scoochworm_footstep")
	val SCOOCHWORM_KISS: DeferredHolder<SoundEvent, SoundEvent> =
		register("scoochworm_kiss")

	private fun register(name: String): DeferredHolder<SoundEvent, SoundEvent> {
		return SOUND_EVENT_REGISTRY.register(name, Supplier {
			SoundEvent.createVariableRangeEvent(CritterCarts.modResource(name))
		})
	}
}