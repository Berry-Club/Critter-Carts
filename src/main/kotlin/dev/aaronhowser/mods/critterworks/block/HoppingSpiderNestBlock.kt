package dev.aaronhowser.mods.critterworks.block

import com.mojang.serialization.MapCodec
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isBlock
import dev.aaronhowser.mods.critterworks.block_entity.HoppingSpiderNestBlockEntity
import dev.aaronhowser.mods.critterworks.registry.ModBlockEntityTypes
import net.minecraft.core.BlockPos
import net.minecraft.world.Containers
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class HoppingSpiderNestBlock(
	properties: Properties = Properties.ofFullCopy(Blocks.OAK_PLANKS)
) : BaseEntityBlock(properties) {

	override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

	override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

	override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
		return HoppingSpiderNestBlockEntity(pos, state)
	}

	override fun useWithoutItem(
		state: BlockState,
		level: Level,
		pos: BlockPos,
		player: Player,
		hitResult: BlockHitResult
	): InteractionResult {
		val nest = level.getBlockEntity(pos) as? HoppingSpiderNestBlockEntity
			?: return InteractionResult.PASS

		if (!level.isClientSide) {
			player.openMenu(nest) { data -> data.writeBlockPos(nest.blockPos) }
		}

		return InteractionResult.sidedSuccess(level.isClientSide)
	}

	override fun onRemove(
		state: BlockState,
		level: Level,
		pos: BlockPos,
		newState: BlockState,
		movedByPiston: Boolean
	) {
		if (!state.isBlock(newState.block)) {
			dropCarriedItems(level, pos)
		}

		super.onRemove(state, level, pos, newState, movedByPiston)
	}

	private fun dropCarriedItems(level: Level, pos: BlockPos) {
		val nest = level.getBlockEntity(pos)
		if (nest !is HoppingSpiderNestBlockEntity) return

		for (spider in nest.hoppingSpiders) {
			if (spider.carriedStack.isEmpty) continue

			Containers.dropItemStack(
				level,
				pos.x + 0.5,
				pos.y + 0.5,
				pos.z + 0.5,
				spider.carriedStack
			)
		}
	}

	override fun <T : BlockEntity> getTicker(
		level: Level,
		state: BlockState,
		blockEntityType: BlockEntityType<T>
	): BlockEntityTicker<T>? {
		if (level.isClientSide) return null

		return createTickerHelper(
			blockEntityType,
			ModBlockEntityTypes.HOPPING_SPIDER_NEST.get(),
			HoppingSpiderNestBlockEntity::serverTick
		)
	}

	companion object {
		val CODEC: MapCodec<HoppingSpiderNestBlock> = simpleCodec(::HoppingSpiderNestBlock)
	}
}