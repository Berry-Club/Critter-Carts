package dev.aaronhowser.mods.critter_carts.world.feature

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.withClickToRunCommand
import dev.aaronhowser.mods.critter_carts.block.ScoochstemBlock
import dev.aaronhowser.mods.critter_carts.registry.ModBlocks
import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.HugeMushroomBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration

class ScoochwormAppleFeature : Feature<NoneFeatureConfiguration>(NoneFeatureConfiguration.CODEC) {

	override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
		val level = context.level()
		val floorPosition = findFloor(level, context.origin()) ?: return false
		val center = floorPosition.above(APPLE_RADIUS)

		if (!isExposed(level, center)) return false

		placeApple(level, center)
		spawnScoochworm(level, center.below(APPLE_RADIUS))
		sendTeleportMessage(level, center)

		return true
	}

	private fun findFloor(level: WorldGenLevel, origin: BlockPos): BlockPos? {
		var position = origin
		var tries = 0

		while (tries < VERTICAL_SEARCH_RANGE && !level.isEmptyBlock(position)) {
			position = position.above()
			tries++
		}

		if (!level.isEmptyBlock(position)) return null

		while (tries < VERTICAL_SEARCH_RANGE * 2 && level.isEmptyBlock(position.below())) {
			position = position.below()
			tries++
		}

		if (level.isEmptyBlock(position.below())) return null

		return position.below()
	}

	private fun isExposed(level: WorldGenLevel, center: BlockPos): Boolean {
		val mutablePosition = BlockPos.MutableBlockPos()

		for (xOffset in -APPLE_RADIUS..APPLE_RADIUS) {
			for (yOffset in -APPLE_RADIUS..APPLE_RADIUS) {
				for (zOffset in -APPLE_RADIUS..APPLE_RADIUS) {
					val internalFace = getInternalFace(xOffset, yOffset, zOffset) ?: continue

					mutablePosition.setWithOffset(center, xOffset, yOffset, zOffset)
					mutablePosition.move(internalFace.opposite)

					if (level.isEmptyBlock(mutablePosition)) return true
				}
			}
		}

		return false
	}

	private fun placeApple(level: WorldGenLevel, center: BlockPos) {
		val mutablePosition = BlockPos.MutableBlockPos()

		for (xOffset in -APPLE_RADIUS..APPLE_RADIUS) {
			for (yOffset in -APPLE_RADIUS..APPLE_RADIUS) {
				for (zOffset in -APPLE_RADIUS..APPLE_RADIUS) {
					mutablePosition.setWithOffset(center, xOffset, yOffset, zOffset)

					val internalFace = getInternalFace(xOffset, yOffset, zOffset)
					val blockState = if (internalFace == null) {
						Blocks.AIR.defaultBlockState()
					} else {
						appleStateWithDisabledFace(internalFace)
					}

					level.setBlock(mutablePosition, blockState, Block.UPDATE_CLIENTS)
				}
			}
		}

		val stemState = ModBlocks.SCOOCHSTEM.get()
			.defaultBlockState()
			.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y)
			.setValue(
				ScoochstemBlock.GROWTH_REMAINING,
				level.random.nextIntBetweenInclusive(ScoochstemBlock.MIN_INITIAL_GROWTH, ScoochstemBlock.MAX_INITIAL_GROWTH)
			)

		val stemPos = center.above(APPLE_RADIUS + 1)

		level.setBlock(
			stemPos,
			stemState,
			Block.UPDATE_CLIENTS
		)

		level.scheduleTick(stemPos, stemState.block, 1)
	}

	private fun spawnScoochworm(level: WorldGenLevel, floorPosition: BlockPos) {
		val scoochworm = ModEntityTypes.SCOOCHWORM.get()
			.spawn(
				level.level,
				floorPosition.above(),
				MobSpawnType.STRUCTURE
			) ?: return

		scoochworm.attachToSupport(floorPosition, Direction.DOWN)
		scoochworm.setPersistenceRequired()
		scoochworm.isTryingToMove = true
	}

	private fun sendTeleportMessage(level: WorldGenLevel, position: BlockPos) {
		val message = Component.literal("[${position.x}, ${position.y}, ${position.z}]")
			.withStyle(
				Style.EMPTY
					.withClickToRunCommand("/tp @s ${position.x} ${position.y} ${position.z}")
			)

		level.level.server.playerList.broadcastSystemMessage(message, false)
	}

	private fun getInternalFace(xOffset: Int, yOffset: Int, zOffset: Int): Direction? {
		var internalFace: Direction? = null
		var boundaryCount = 0

		if (xOffset == -APPLE_RADIUS || xOffset == APPLE_RADIUS) {
			internalFace = if (xOffset < 0) Direction.EAST else Direction.WEST
			boundaryCount++
		}

		if (yOffset == -APPLE_RADIUS || yOffset == APPLE_RADIUS) {
			internalFace = if (yOffset < 0) Direction.UP else Direction.DOWN
			boundaryCount++
		}

		if (zOffset == -APPLE_RADIUS || zOffset == APPLE_RADIUS) {
			internalFace = if (zOffset < 0) Direction.SOUTH else Direction.NORTH
			boundaryCount++
		}

		return if (boundaryCount == 1) internalFace else null
	}

	private fun appleStateWithDisabledFace(internalFace: Direction): BlockState {
		val property: BooleanProperty = when (internalFace) {
			Direction.NORTH -> HugeMushroomBlock.NORTH
			Direction.EAST -> HugeMushroomBlock.EAST
			Direction.SOUTH -> HugeMushroomBlock.SOUTH
			Direction.WEST -> HugeMushroomBlock.WEST
			Direction.UP -> HugeMushroomBlock.UP
			Direction.DOWN -> HugeMushroomBlock.DOWN
		}

		return ModBlocks.APPLE_SLICE.get()
			.defaultBlockState()
			.setValue(property, false)
	}

	companion object {
		private const val APPLE_RADIUS = 2
		private const val VERTICAL_SEARCH_RANGE = 24
	}
}