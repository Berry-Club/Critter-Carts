package dev.aaronhowser.mods.critter_carts.block

import com.mojang.serialization.MapCodec
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isBlock
import dev.aaronhowser.mods.critter_carts.block_entity.HoppingSpiderNestBlockEntity
import dev.aaronhowser.mods.critter_carts.registry.ModBlockEntityTypes
import net.minecraft.core.BlockPos
import net.minecraft.world.Containers
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

class HoppingSpiderNestBlock(
	properties: Properties = Properties.ofFullCopy(Blocks.OAK_PLANKS)
) : BaseEntityBlock(properties) {

	override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

	override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

	override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
		return HoppingSpiderNestBlockEntity(pos, state)
	}

	override fun onRemove(
		state: BlockState,
		level: Level,
		pos: BlockPos,
		newState: BlockState,
		movedByPiston: Boolean
	) {
		if (!state.isBlock(newState.block)) {
			val blockEntity = level.getBlockEntity(pos)
			if (blockEntity is HoppingSpiderNestBlockEntity) {
				for (spider in blockEntity.hoppingSpiders) {
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
		}

		super.onRemove(state, level, pos, newState, movedByPiston)
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