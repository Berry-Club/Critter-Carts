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
import net.neoforged.neoforge.client.model.generators.ModelBuilder
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
		val sideHorizontalModel = scoochstemFaceModel(
			"scoochstem_side_horizontal",
			side,
			true
		)
		val sideHorizontalDisabledModel = scoochstemFaceModel(
			"scoochstem_side_horizontal_disabled",
			sideDisabled,
			true
		)
		val topModel = scoochstemFaceModel("scoochstem_top", top)
		val topDisabledModel = scoochstemFaceModel("scoochstem_top_disabled", topDisabled)
		val topHorizontalModel = scoochstemFaceModel(
			"scoochstem_top_horizontal",
			top,
			true
		)
		val topHorizontalDisabledModel = scoochstemFaceModel(
			"scoochstem_top_horizontal_disabled",
			topDisabled,
			true
		)

		val multipartBuilder = getMultipartBuilder(scoochstem)

		for (direction in Direction.entries) {
			for (axis in Direction.Axis.entries) {
				val isTop = direction.axis == axis
				val enabledModel = when {
					isTop && axis == Direction.Axis.Y -> topModel
					isTop -> topHorizontalModel
					axis == Direction.Axis.Y -> sideModel
					else -> sideHorizontalModel
				}
				val disabledModel = when {
					isTop && axis == Direction.Axis.Y -> topDisabledModel
					isTop -> topHorizontalDisabledModel
					axis == Direction.Axis.Y -> sideDisabledModel
					else -> sideHorizontalDisabledModel
				}
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
		texture: ResourceLocation,
		rotateTexture: Boolean = false
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

					if (rotateTexture) {
						rotation(ModelBuilder.FaceRotation.CLOCKWISE_90)
					}
				}
			}
	}

	private fun getFaceXRotation(direction: Direction): Int {
		return when (direction) {
			Direction.UP -> 270
			Direction.DOWN -> 90
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