package dev.aaronhowser.mods.critter_carts.datagen.sound

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.registry.ModSoundEvents
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.ExistingFileHelper
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider

class ModSoundDefinitionsProvider(
	output: PackOutput,
	existingFileHelper: ExistingFileHelper
) : SoundDefinitionsProvider(output, CritterCarts.MOD_ID, existingFileHelper) {

	override fun registerSounds() {
		val definition = definition()

		for (variation in 1..FOOTSTEP_VARIATIONS) {
			definition.with(
				sound(CritterCarts.modResource("entity/scoochworm/footstep_$variation"))
			)
		}

		add(ModSoundEvents.SCOOCHWORM_FOOTSTEP, definition)
	}

	companion object {
		private const val FOOTSTEP_VARIATIONS = 5
	}
}