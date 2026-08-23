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
		scoochstem()
		coloredScoochstems()
	}

	private fun coloredScoochstems() {
		val topTexture = modLoc("block/scoochstem/top")
		val blocks = listOf(
			ModBlocks.GREEN_SCOOCHSTEM.get(),
			ModBlocks.BLUE_SCOOCHSTEM.get(),
			ModBlocks.RED_SCOOCHSTEM.get(),
			ModBlocks.YELLOW_SCOOCHSTEM.get(),
			ModBlocks.PURPLE_SCOOCHSTEM.get(),
			ModBlocks.CYAN_SCOOCHSTEM.get()
		)

		for (block in blocks) {
			val color = block.color
			val name = "${color.color}_scoochstem"
			val sideTexture = modLoc("block/colored_scoochstem/${color.color}")

			val model = models()
				.cube(name, topTexture, topTexture, sideTexture, sideTexture, sideTexture, sideTexture)
				.particle(sideTexture)

			axisBlock(block, model, model)
			simpleBlockItem(block, model)
		}
	}

	private fun scoochstem() {
		val scoochstem = ModBlocks.SCOOCHSTEM.get()
		val side = modLoc("block/scoochstem/side")
		val sideDisabled = modLoc("block/scoochstem/side_disabled")
		val top = modLoc("block/scoochstem/top")
		val topDisabled = modLoc("block/scoochstem/top_disabled")

		val sideModels = scoochstemFaceModels("scoochstem_side", side)
		val disabledSideModels = scoochstemFaceModels("scoochstem_side_disabled", sideDisabled)
		val topModels = scoochstemFaceModels("scoochstem_top", top)
		val disabledTopModels = scoochstemFaceModels("scoochstem_top_disabled", topDisabled)

		val multipartBuilder = getMultipartBuilder(scoochstem)

		for (direction in Direction.entries) {
			for (axis in Direction.Axis.entries) {
				for (disabled in listOf(false, true)) {
					val isCap = direction.axis == axis
					val faceModels = when {
						isCap && disabled -> disabledTopModels
						isCap -> topModels
						disabled -> disabledSideModels
						else -> sideModels
					}
					val model = if (shouldRotateTexture(axis, direction)) {
						faceModels.second
					} else {
						faceModels.first
					}

					multipartBuilder
						.part()
						.modelFile(model)
						.rotationX(getFaceXRotation(direction))
						.rotationY(getFaceYRotation(direction))
						.addModel()
						.condition(RotatedPillarBlock.AXIS, axis)
						.condition(ScoochstemBlock.getDisabledProperty(direction), disabled)
						.end()
				}
			}
		}

		val itemModel = models()
			.cube("scoochstem", top, top, side, side, side, side)
			.particle(side)

		simpleBlockItem(scoochstem, itemModel)

		scoochstemWood(sideModels, disabledSideModels, side)
	}

	private fun scoochstemWood(
		sideModels: Pair<BlockModelBuilder, BlockModelBuilder>,
		disabledSideModels: Pair<BlockModelBuilder, BlockModelBuilder>,
		sideTexture: ResourceLocation
	) {
		val scoochstemWood = ModBlocks.SCOOCHSTEM_WOOD.get()
		val multipartBuilder = getMultipartBuilder(scoochstemWood)

		for (direction in Direction.entries) {
			for (axis in Direction.Axis.entries) {
				for (disabled in listOf(false, true)) {
					val faceModels = if (disabled) disabledSideModels else sideModels
					val model = if (shouldRotateTexture(axis, direction)) {
						faceModels.second
					} else {
						faceModels.first
					}

					multipartBuilder
						.part()
						.modelFile(model)
						.rotationX(getFaceXRotation(direction))
						.rotationY(getFaceYRotation(direction))
						.addModel()
						.condition(RotatedPillarBlock.AXIS, axis)
						.condition(ScoochstemBlock.getDisabledProperty(direction), disabled)
						.end()
				}
			}
		}

		val itemModel = models()
			.cubeAll("scoochstem_wood", sideTexture)
			.particle(sideTexture)

		simpleBlockItem(scoochstemWood, itemModel)
	}

	private fun scoochstemFaceModels(
		name: String,
		texture: ResourceLocation
	): Pair<BlockModelBuilder, BlockModelBuilder> {
		val regular = scoochstemFaceModel(name, texture, false)
		val rotated = scoochstemFaceModel(name + "_rotated", texture, true)

		return regular to rotated
	}

	private fun scoochstemFaceModel(
		name: String,
		texture: ResourceLocation,
		rotateTexture: Boolean
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

	private fun shouldRotateTexture(
		axis: Direction.Axis,
		direction: Direction
	): Boolean {
		if (direction.axis == axis) return axis != Direction.Axis.Y
		if (axis == Direction.Axis.Y) return false
		if (direction.axis == Direction.Axis.Y) return axis == Direction.Axis.X

		return true
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