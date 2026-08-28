package dev.aaronhowser.mods.critter_carts.block

import dev.aaronhowser.mods.critter_carts.block_entity.CritterCageBlockEntity
import dev.aaronhowser.mods.critter_carts.registry.ModDataComponents
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.DirectionProperty
import net.minecraft.world.phys.BlockHitResult

class CritterCageBlock : Block(
	Properties.ofFullCopy(Blocks.IRON_BARS).noOcclusion()
), EntityBlock {

	init {
		registerDefaultState(
			stateDefinition.any()
				.setValue(DOWN, Direction.DOWN)
				.setValue(FORWARD, Direction.NORTH)
				.setValue(OPEN, true)
				.setValue(POWERED, false)
		)
	}

	override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
		builder.add(DOWN, FORWARD, OPEN, POWERED)
	}

	override fun getStateForPlacement(context: BlockPlaceContext): BlockState {
		val down = context.clickedFace.opposite
		val forward = context.nearestLookingDirections.first { direction ->
			direction.axis != down.axis
		}.opposite

		return defaultBlockState()
			.setValue(DOWN, down)
			.setValue(FORWARD, forward)
			.setValue(OPEN, true)
			.setValue(POWERED, context.level.hasNeighborSignal(context.clickedPos))
	}

	override fun setPlacedBy(
		level: Level,
		pos: BlockPos,
		state: BlockState,
		placer: LivingEntity?,
		stack: ItemStack
	) {
		super.setPlacedBy(level, pos, state, placer, stack)
		val blockEntity = level.getBlockEntity(pos)
		if (blockEntity is CritterCageBlockEntity) {
			blockEntity.entityData = stack.get(ModDataComponents.ENTITY_DATA)
			level.setBlock(pos, state.setValue(OPEN, !blockEntity.hasEntity), UPDATE_CLIENTS)
		}
	}

	override fun useWithoutItem(
		state: BlockState,
		level: Level,
		pos: BlockPos,
		player: Player,
		hitResult: BlockHitResult
	): InteractionResult {
		val blockEntity = level.getBlockEntity(pos)
		if (blockEntity !is CritterCageBlockEntity || !blockEntity.hasEntity) {
			return InteractionResult.PASS
		}

		if (!level.isClientSide) {
			blockEntity.tryRelease(player)
		}

		return InteractionResult.sidedSuccess(level.isClientSide)
	}

	override fun neighborChanged(
		state: BlockState,
		level: Level,
		pos: BlockPos,
		neighborBlock: Block,
		neighborPos: BlockPos,
		movedByPiston: Boolean
	) {
		val powered = level.hasNeighborSignal(pos)
		if (powered == state.getValue(POWERED)) return

		level.setBlock(pos, state.setValue(POWERED, powered), UPDATE_CLIENTS)
		if (!powered || level.isClientSide) return

		val blockEntity = level.getBlockEntity(pos)
		if (blockEntity is CritterCageBlockEntity) {
			blockEntity.tryRelease(null)
		}
	}

	override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

	override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
		return CritterCageBlockEntity(pos, state)
	}

	companion object {
		val DOWN: DirectionProperty = DirectionProperty.create("down")
		val FORWARD: DirectionProperty = DirectionProperty.create("forward")
		val OPEN: BooleanProperty = BlockStateProperties.OPEN
		val POWERED: BooleanProperty = BlockStateProperties.POWERED
	}
}