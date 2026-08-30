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
		val footstepDefinition = definition()
			.subtitle(ModSoundEvents.SCOOCHWORM_FOOTSTEP_SUBTITLE)
			.with(
				sound(CritterCarts.modResource("entity/scoochworm/footstep_1"))
			)
			.with(
				sound(CritterCarts.modResource("entity/scoochworm/footstep_2"))
			)

		add(ModSoundEvents.SCOOCHWORM_FOOTSTEP, footstepDefinition)

		val kissDefinition = definition()
			.subtitle(ModSoundEvents.SCOOCHWORM_KISS_SUBTITLE)
			.with(
				sound(CritterCarts.modResource("entity/kiss_1"))
			)
			.with(
				sound(CritterCarts.modResource("entity/kiss_2"))
			)
			.with(
				sound(CritterCarts.modResource("entity/kiss_3"))
			)

		add(ModSoundEvents.SCOOCHWORM_KISS, kissDefinition)
	}

}