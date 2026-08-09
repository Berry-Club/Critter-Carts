package dev.aaronhowser.mods.critter_carts.datagen.model

import dev.aaronhowser.mods.aaron.misc.AaronDsls.element
import dev.aaronhowser.mods.aaron.misc.AaronDsls.face
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.particle
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.block.ScoochstemBlock
import dev.aaronhowser.mods.critter_carts.registry.ModBlocks
import net.minecraft.core.Direction
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.RotatedPillarBlock
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder
import net.neoforged.neoforge.client.model.generators.BlockStateProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper

class ModBlockStateProvider(
	output: PackOutput,
	existingFileHelper: ExistingFileHelper
) : BlockStateProvider(output, CritterCarts.MOD_ID, existingFileHelper) {

	override fun registerStatesAndModels() {
		val scoochstem = ModBlocks.SCOOCHSTEM.get()
		val side = CritterCarts.modResource("block/scoochstem/side")
		val sideDisabled = CritterCarts.modResource("block/scoochstem/side_disabled")
		val top = CritterCarts.modResource("block/scoochstem/top")
		val topDisabled = CritterCarts.modResource("block/scoochstem/top_disabled")

		val sideModel = scoochstemFaceModel("scoochstem_side", side)
		val sideDisabledModel = scoochstemFaceModel("scoochstem_side_disabled", sideDisabled)
		val topModel = scoochstemFaceModel("scoochstem_top", top)
		val topDisabledModel = scoochstemFaceModel("scoochstem_top_disabled", topDisabled)

		val multipartBuilder = getMultipartBuilder(scoochstem)

		for (direction in Direction.entries) {
			for (axis in Direction.Axis.entries) {
				val isTop = direction.axis == axis
				val enabledModel = if (isTop) topModel else sideModel
				val disabledModel = if (isTop) topDisabledModel else sideDisabledModel
				val disabledProperty = ScoochstemBlock.getDisabledProperty(direction)
				val xRotation = getFaceXRotation(direction)
				val yRotation = getFaceYRotation(direction)

				multipartBuilder
					.part()
					.modelFile(enabledModel)
					.rotationX(xRotation)
					.rotationY(yRotation)
					.addModel()
					.condition(RotatedPillarBlock.AXIS, axis)
					.condition(disabledProperty, false)
					.end()

				multipartBuilder
					.part()
					.modelFile(disabledModel)
					.rotationX(xRotation)
					.rotationY(yRotation)
					.addModel()
					.condition(RotatedPillarBlock.AXIS, axis)
					.condition(disabledProperty, true)
					.end()
			}
		}

		val itemModel = models()
			.cube("scoochstem", top, top, side, side, side, side)
			.particle(side)

		simpleBlockItem(scoochstem, itemModel)
	}

	private fun scoochstemFaceModel(
		name: String,
		texture: ResourceLocation
	): BlockModelBuilder {
		return models()
			.withExistingParent(name, mcLoc("block/block"))
			.texture("texture", texture)
			.particle(texture)
			.element {
				from(0f, 0f, 0f)
				to(16f, 16f, 16f)
				face(Direction.NORTH) {
					texture("#texture")
					cullface(Direction.NORTH)
				}
			}
	}

	private fun getFaceXRotation(direction: Direction): Int {
		return when (direction) {
			Direction.UP -> 90
			Direction.DOWN -> 270
			else -> 0
		}
	}

	private fun getFaceYRotation(direction: Direction): Int {
		return when (direction) {
			Direction.NORTH -> 0
			Direction.EAST -> 90
			Direction.SOUTH -> 180
			Direction.WEST -> 270
			else -> 0
		}
	}
}