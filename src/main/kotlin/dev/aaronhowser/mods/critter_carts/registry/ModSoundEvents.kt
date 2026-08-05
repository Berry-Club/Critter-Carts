package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.critter_carts.CritterCarts
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.sounds.SoundEvent
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object ModSoundEvents {

	val SOUND_EVENT_REGISTRY: DeferredRegister<SoundEvent> =
		DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, CritterCarts.MOD_ID)

	val SCOOCHWORM_FOOTSTEP: DeferredHolder<SoundEvent, SoundEvent> =
		register("scoochworm_footstep")

	private fun register(name: String): DeferredHolder<SoundEvent, SoundEvent> {
		return SOUND_EVENT_REGISTRY.register(name, Supplier {
			SoundEvent.createVariableRangeEvent(CritterCarts.modResource(name))
		})
	}
}