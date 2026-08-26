package dev.aaronhowser.mods.critter_carts.datagen.model

import dev.aaronhowser.mods.aaron.misc.AaronDsls.element
import dev.aaronhowser.mods.aaron.misc.AaronDsls.face
import dev.aaronhowser.mods.aaron.misc.AaronDsls.transform
import dev.aaronhowser.mods.aaron.misc.AaronDsls.transforms
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.particle
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.block.CritterCageBlock
import dev.aaronhowser.mods.critter_carts.block.ScoochstemBlock
import dev.aaronhowser.mods.critter_carts.registry.ModBlocks
import dev.aaronhowser.mods.critter_carts.registry.ModItems
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.Direction
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.level.block.HugeMushroomBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder
import net.neoforged.neoforge.client.model.generators.BlockStateProvider
import net.neoforged.neoforge.client.model.generators.ConfiguredModel
import net.neoforged.neoforge.client.model.generators.ModelBuilder
import net.neoforged.neoforge.client.model.generators.ModelFile
import net.neoforged.neoforge.common.data.ExistingFileHelper

class ModBlockStateProvider(
	output: PackOutput,
	existingFileHelper: ExistingFileHelper
) : BlockStateProvider(output, CritterCarts.MOD_ID, existingFileHelper) {

	override fun registerStatesAndModels() {
		critterCage()
		appleSlice()
		scoochstem()
		coloredScoochstems()
	}

	private fun critterCage() {
		val bottomTexture = modLoc("block/critter_cage/critter_cage_bottom")
		val sideTexture = modLoc("block/critter_cage/critter_cage_side")
		val topTexture = modLoc("block/critter_cage/critter_cage_top")
		val model = models()
			.withExistingParent("critter_cage_block", mcLoc("block/block"))
			.renderType(RenderType.CUTOUT.name)
			.texture("bottom", bottomTexture)
			.texture("side", sideTexture)
			.texture("top", topTexture)
			.particle(sideTexture)

		for (direction in Direction.entries) {
			val texture = when (direction) {
				Direction.DOWN -> "#bottom"
				Direction.UP -> "#top"
				else -> "#side"
			}

			model.element {
				when (direction) {
					Direction.DOWN -> {
						from(0f, 0f, 0f)
						to(16f, 0f, 16f)
					}

					Direction.UP -> {
						from(0f, 16f, 0f)
						to(16f, 16f, 16f)
					}

					Direction.NORTH -> {
						from(0f, 0f, 0f)
						to(16f, 16f, 0f)
					}

					Direction.SOUTH -> {
						from(0f, 0f, 16f)
						to(16f, 16f, 16f)
					}

					Direction.WEST -> {
						from(0f, 0f, 0f)
						to(0f, 16f, 16f)
					}

					Direction.EAST -> {
						from(16f, 0f, 0f)
						to(16f, 16f, 16f)
					}
				}

				face(direction) {
					texture(texture)
				}
				face(direction.opposite) {
					texture(texture)
				}
			}
		}

		getVariantBuilder(ModBlocks.CRITTER_CAGE.get()).forAllStates { state ->
			val down = state.getValue(CritterCageBlock.DOWN)
			val forward = state.getValue(CritterCageBlock.FORWARD)
			val configuredModel = ConfiguredModel.builder().modelFile(model)

			when (down) {
				Direction.DOWN -> configuredModel
					.rotationY(horizontalRotation(forward))
				Direction.UP -> configuredModel
					.rotationX(180)
					.rotationY((horizontalRotation(forward) + 180) % 360)
				Direction.NORTH -> configuredModel.rotationX(90)
				Direction.EAST -> configuredModel.rotationX(90).rotationY(90)
				Direction.SOUTH -> configuredModel.rotationX(90).rotationY(180)
				Direction.WEST -> configuredModel.rotationX(90).rotationY(270)
			}

			configuredModel.build()
		}

		itemModels()
			.getBuilder(ModItems.CRITTER_CAGE.id.path)
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

	private fun horizontalRotation(direction: Direction): Int {
		return when (direction) {
			Direction.NORTH -> 0
			Direction.EAST -> 90
			Direction.SOUTH -> 180
			Direction.WEST -> 270
			else -> 0
		}
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