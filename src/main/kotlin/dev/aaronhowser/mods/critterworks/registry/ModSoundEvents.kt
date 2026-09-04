package dev.aaronhowser.mods.critterworks.registry

import dev.aaronhowser.mods.critterworks.Critterworks
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.sounds.SoundEvent
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object ModSoundEvents {
	val SOUND_EVENT_REGISTRY: DeferredRegister<SoundEvent> =
		DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Critterworks.MOD_ID)

	val SCOOCHWORM_FOOTSTEP: DeferredHolder<SoundEvent, SoundEvent> =
		register("scoochworm_footstep")
	val SCOOCHWORM_KISS: DeferredHolder<SoundEvent, SoundEvent> =
		register("scoochworm_kiss")
	val WEB_SNAP: DeferredHolder<SoundEvent, SoundEvent> =
		register("web_snap")

	private fun register(name: String): DeferredHolder<SoundEvent, SoundEvent> {
		return SOUND_EVENT_REGISTRY.register(name, Supplier {
			SoundEvent.createVariableRangeEvent(Critterworks.modResource(name))
		})
	}
}