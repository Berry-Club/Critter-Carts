package dev.aaronhowser.mods.critter_carts.datagen.model

import dev.aaronhowser.mods.aaron.misc.AaronDsls.transform
import dev.aaronhowser.mods.aaron.misc.AaronDsls.transforms
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.registry.ModItems
import net.minecraft.data.PackOutput
import net.minecraft.world.item.ItemDisplayContext
import net.neoforged.neoforge.client.model.generators.ItemModelProvider
import net.neoforged.neoforge.client.model.generators.ModelFile
import net.neoforged.neoforge.common.data.ExistingFileHelper

class ModItemModelProvider(
	output: PackOutput,
	existingFileHelper: ExistingFileHelper
) : ItemModelProvider(output, CritterCarts.MOD_ID, existingFileHelper) {

	override fun registerModels() {
		basicItem(ModItems.SADDLEBAG.get())
		basicItem(ModItems.WICKER_BASKET.get())
		critterCage()
		spawnEggItem(ModItems.SCOOCHWORM_SPAWN_EGG.get())
	}

	private fun critterCage() {
		val cage = ModItems.CRITTER_CAGE.get()
		val cageTexture = modLoc("item/critter_cage")

		getBuilder("critter_cage_base")
			.parent(ModelFile.UncheckedModelFile("builtin/generated"))
			.texture("layer0", cageTexture)

		getBuilder(cage.descriptionId.substringAfterLast('.'))
			.parent(ModelFile.UncheckedModelFile("builtin/entity"))
			.transforms {
				transform(ItemDisplayContext.GROUND) {
					translation(0f, 2f, 0f)
					scale(0.5f)
				}

				transform(ItemDisplayContext.HEAD) {
					rotation(0f, 180f, 0f)
					translation(0f, 13f, 7f)
				}

				transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
					translation(0f, 3f, 1f)
					scale(0.55f)
				}

				transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
					rotation(0f, -90f, 25f)
					translation(1.13f, 3.2f, 1.13f)
					scale(0.68f)
				}

				transform(ItemDisplayContext.FIXED) {
					rotation(0f, 180f, 0f)
				}
			}
	}
}