package dev.aaronhowser.mods.critterworks.block

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isItem
import dev.aaronhowser.mods.critterworks.block.base.ScoochwormSegmentSupportBlock
import dev.aaronhowser.mods.critterworks.entity.ScoochwormEntity
import dev.aaronhowser.mods.critterworks.entity.ScoochwormPartEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.neoforged.neoforge.items.ItemHandlerHelper

class StemEncasedComparatorBlock : ScoochstemBlock(), ScoochwormSegmentSupportBlock {

	override fun useItemOn(
		stack: ItemStack,
		state: BlockState,
		level: Level,
		position: BlockPos,
		player: Player,
		hand: InteractionHand,
		hitResult: BlockHitResult
	): ItemInteractionResult {
		if (stack.isItem(Items.BONE_MEAL)) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
		}

		return super.useItemOn(stack, state, level, position, player, hand, hitResult)
	}

	override fun isSignalSource(state: BlockState): Boolean = true

	override fun getSignal(
		state: BlockState,
		level: BlockGetter,
		position: BlockPos,
		direction: Direction
	): Int {
		if (level !is Level) return 0

		return calculateOutputSignal(level, position)
	}

	override fun onSegmentTick(
		state: BlockState,
		level: ServerLevel,
		position: BlockPos,
		segment: ScoochwormPartEntity
	) {
		notifyNeighbors(level, position)
	}

	override fun onSegmentDetached(
		state: BlockState,
		level: ServerLevel,
		position: BlockPos,
		segment: ScoochwormPartEntity
	) {
		level.scheduleTick(position, this, 1)
	}

	private fun notifyNeighbors(level: ServerLevel, position: BlockPos) {
		level.updateNeighborsAt(position, this)
	}

	override fun tick(
		state: BlockState,
		level: ServerLevel,
		position: BlockPos,
		random: RandomSource
	) {
		super.tick(state, level, position, random)
		notifyNeighbors(level, position)
	}

	private fun calculateOutputSignal(level: Level, position: BlockPos): Int {
		val searchBounds = AABB(position).inflate(ScoochwormEntity.SIZE.toDouble())
		val bodyParts = level.getEntitiesOfClass(ScoochwormPartEntity::class.java, searchBounds)
		var strongestSignal = 0

		for (bodyPart in bodyParts) {
			val supportPosition = ScoochwormEntity.getSupportBlockPosition(
				bodyPart.position(),
				bodyPart.supportDirection
			)
			if (supportPosition != position) continue

			val signal = ItemHandlerHelper.calcRedstoneFromInventory(bodyPart.getItemHandler())
			strongestSignal = maxOf(strongestSignal, signal)
		}

		return strongestSignal
	}

}