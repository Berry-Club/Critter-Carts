package dev.aaronhowser.mods.critter_carts.block

import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.neoforged.neoforge.common.ItemAbilities
import net.neoforged.neoforge.common.ItemAbility

class ScoochstemBlock : RotatedPillarBlock(Properties.ofFullCopy(Blocks.OAK_LOG)), ScoochwormTravelBlock {

	init {
		registerDefaultState(
			defaultBlockState()
				.setValue(NORTH_DISABLED, false)
				.setValue(EAST_DISABLED, false)
				.setValue(SOUTH_DISABLED, false)
				.setValue(WEST_DISABLED, false)
				.setValue(UP_DISABLED, false)
				.setValue(DOWN_DISABLED, false)
		)
	}

	override fun createBlockStateDefinition(
		builder: StateDefinition.Builder<Block, BlockState>
	) {
		super.createBlockStateDefinition(builder)
		builder.add(
			NORTH_DISABLED,
			EAST_DISABLED,
			SOUTH_DISABLED,
			WEST_DISABLED,
			UP_DISABLED,
			DOWN_DISABLED
		)
	}

	override fun getToolModifiedState(
		state: BlockState,
		context: UseOnContext,
		itemAbility: ItemAbility,
		simulate: Boolean
	): BlockState? {
		if (itemAbility != ItemAbilities.AXE_STRIP) return null

		val disabledProperty = getDisabledProperty(context.clickedFace)
		return state.setValue(disabledProperty, !state.getValue(disabledProperty))
	}

	override fun supportsScoochwormTravel(
		blockState: BlockState,
		scoochworm: ScoochwormEntity,
		level: Level,
		position: BlockPos,
		face: Direction
	): Boolean {
		return !blockState.getValue(getDisabledProperty(face))
	}

	companion object {
		val NORTH_DISABLED: BooleanProperty = BooleanProperty.create("north_disabled")
		val EAST_DISABLED: BooleanProperty = BooleanProperty.create("east_disabled")
		val SOUTH_DISABLED: BooleanProperty = BooleanProperty.create("south_disabled")
		val WEST_DISABLED: BooleanProperty = BooleanProperty.create("west_disabled")
		val UP_DISABLED: BooleanProperty = BooleanProperty.create("up_disabled")
		val DOWN_DISABLED: BooleanProperty = BooleanProperty.create("down_disabled")

		fun getDisabledProperty(direction: Direction): BooleanProperty {
			return when (direction) {
				Direction.NORTH -> NORTH_DISABLED
				Direction.EAST -> EAST_DISABLED
				Direction.SOUTH -> SOUTH_DISABLED
				Direction.WEST -> WEST_DISABLED
				Direction.UP -> UP_DISABLED
				Direction.DOWN -> DOWN_DISABLED
			}
		}
	}

}