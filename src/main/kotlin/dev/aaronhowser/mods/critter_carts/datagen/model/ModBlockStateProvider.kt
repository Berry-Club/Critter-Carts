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
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HugeMushroomBlock
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
		appleSlice()
		scoochstem()
		coloredScoochstems()
	}

	private fun appleSlice() {
		val block = ModBlocks.APPLE_SLICE.get()
		val outsideModel = models().getExistingFile(mcLoc("block/red_mushroom_block"))

		for (direction in Direction.entries) {
			val property = when (direction) {
				Direction.NORTH -> HugeMushroomBlock.NORTH
				Direction.EAST -> HugeMushroomBlock.EAST
				Direction.SOUTH -> HugeMushroomBlock.SOUTH
				Direction.WEST -> HugeMushroomBlock.WEST
				Direction.UP -> HugeMushroomBlock.UP
				Direction.DOWN -> HugeMushroomBlock.DOWN
			}

			getMultipartBuilder(block)
				.part()
				.modelFile(outsideModel)
				.rotationX(getFaceXRotation(direction))
				.rotationY(getFaceYRotation(direction))
				.addModel()
				.condition(property, true)
				.end()
		}

		getMultipartBuilder(block)
			.part()
			.modelFile(models().getExistingFile(mcLoc("block/mushroom_block_inside")))
			.addModel()
			.condition(HugeMushroomBlock.NORTH, false)
			.condition(HugeMushroomBlock.EAST, false)
			.condition(HugeMushroomBlock.SOUTH, false)
			.condition(HugeMushroomBlock.WEST, false)
			.condition(HugeMushroomBlock.UP, false)
			.condition(HugeMushroomBlock.DOWN, false)
			.end()

		simpleBlockItem(block, models().cubeAll("apple_slice", mcLoc("block/red_mushroom_block")))
	}

	private fun coloredScoochstems() {
		val topTexture = modLoc("block/scoochstem/top")
		val blocks = listOf(
			ModBlocks.GREEN_SCOOCHSTEM.get(),
			ModBlocks.BLUE_SCOOCHSTEM.get(),
			ModBlocks.RED_SCOOCHSTEM.get(),
			ModBlocks.YELLOW_SCOOCHSTEM.get(),
			ModBlocks.MAGENTA_SCOOCHSTEM.get(),
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

		val sideModels =
			scoochstemFaceModels("scoochstem_side", side)
		val disabledSideModels =
			scoochstemFaceModels("scoochstem_side_disabled", sideDisabled)
		val endModels =
			scoochstemFaceModels("scoochstem_top", top)
		val disabledEndModels =
			scoochstemFaceModels("scoochstem_top_disabled", topDisabled)

		scoochstemBlock(
			block = scoochstem,
			sideModels = sideModels,
			disabledSideModels = disabledSideModels,
			endModels = endModels,
			disabledEndModels = disabledEndModels
		)

		val itemModel = models()
			.cube("scoochstem", top, top, side, side, side, side)
			.particle(side)

		simpleBlockItem(scoochstem, itemModel)

		val scoochstemWood = ModBlocks.SCOOCHSTEM_WOOD.get()
		scoochstemBlock(
			block = scoochstemWood,
			sideModels = sideModels,
			disabledSideModels = disabledSideModels,
			endModels = sideModels,
			disabledEndModels = disabledSideModels
		)

		val woodItemModel = models()
			.cubeAll("scoochstem_wood", side)
			.particle(side)

		simpleBlockItem(scoochstemWood, woodItemModel)
	}

	private fun scoochstemBlock(
		block: Block,
		sideModels: Pair<BlockModelBuilder, BlockModelBuilder>,
		disabledSideModels: Pair<BlockModelBuilder, BlockModelBuilder>,
		endModels: Pair<BlockModelBuilder, BlockModelBuilder>,
		disabledEndModels: Pair<BlockModelBuilder, BlockModelBuilder>
	) {
		fun addScoochstemFace(
			direction: Direction,
			axis: Direction.Axis,
			disabled: Boolean,
			faceModels: Pair<BlockModelBuilder, BlockModelBuilder>
		) {
			val shouldRotateTexture = when {
				direction.axis == axis -> axis != Direction.Axis.Y
				axis == Direction.Axis.Y -> false
				direction.axis == Direction.Axis.Y -> axis == Direction.Axis.X
				else -> true
			}

			val faceModel = if (shouldRotateTexture) {
				faceModels.second
			} else {
				faceModels.first
			}

			getMultipartBuilder(block)
				.part()
				.modelFile(faceModel)
				.rotationX(getFaceXRotation(direction))
				.rotationY(getFaceYRotation(direction))
				.addModel()
				.condition(RotatedPillarBlock.AXIS, axis)
				.condition(ScoochstemBlock.getDisabledProperty(direction), disabled)
				.end()
		}

		for (direction in Direction.entries) {
			for (axis in Direction.Axis.entries) {
				val isPillarEnd = direction.axis == axis
				val enabledModels =
					if (isPillarEnd) endModels else sideModels
				val disabledModels =
					if (isPillarEnd) disabledEndModels else disabledSideModels

				addScoochstemFace(
					direction = direction,
					axis = axis,
					disabled = false,
					faceModels = enabledModels
				)
				addScoochstemFace(
					direction = direction,
					axis = axis,
					disabled = true,
					faceModels = disabledModels
				)
			}
		}
	}

	private fun scoochstemFaceModels(
		name: String,
		texture: ResourceLocation
	): Pair<BlockModelBuilder, BlockModelBuilder> {
		fun scoochstemFaceModel(
			modelName: String,
			rotateTexture: Boolean
		): BlockModelBuilder {
			return models()
				.withExistingParent(modelName, mcLoc("block/block"))
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

		val regularModel = scoochstemFaceModel(name, false)
		val rotatedModel = scoochstemFaceModel(name + "_rotated", true)

		return regularModel to rotatedModel
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