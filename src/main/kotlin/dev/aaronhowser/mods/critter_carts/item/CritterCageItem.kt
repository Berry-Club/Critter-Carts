package dev.aaronhowser.mods.critter_carts.item

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.getMinimalTag
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isClientSide
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isServerSide
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import dev.aaronhowser.mods.critter_carts.registry.ModDataComponents
import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level

class CritterCageItem(properties: Properties) : Item(properties) {

	override fun interactLivingEntity(
		stack: ItemStack,
		player: Player,
		interactionTarget: LivingEntity,
		usedHand: InteractionHand
	): InteractionResult {
		if (interactionTarget !is ScoochwormEntity || stack.has(ModDataComponents.ENTITY_DATA)) return InteractionResult.PASS

		if (player.isServerSide) {
			val wormNbt = interactionTarget.getMinimalTag(stripUniqueness = false)
			stack.set(ModDataComponents.ENTITY_DATA, CustomData.of(wormNbt))

			interactionTarget.discard()
		}

		return InteractionResult.sidedSuccess(player.isClientSide)
	}

	override fun useOn(context: UseOnContext): InteractionResult {
		val stack = context.itemInHand
		if (!stack.has(ModDataComponents.ENTITY_DATA)) return InteractionResult.PASS

		val level = context.level

		val clickedPos = context.clickedPos
		val clickedFace = context.clickedFace
		val clickedState = level.getBlockState(clickedPos)

		val posToSpawn = if (clickedState.isSuffocating(level, clickedPos)) {
			val relative = clickedPos.relative(clickedFace)
			if (level.getBlockState(relative).isSuffocating(level, relative)) {
				return InteractionResult.FAIL
			}

			relative
		} else {
			clickedPos
		}

		val success = placeScoochworm(stack, level, posToSpawn) != null
		if (!success) return InteractionResult.FAIL

		stack.remove(ModDataComponents.ENTITY_DATA)

		return InteractionResult.SUCCESS
	}

	override fun isFoil(stack: ItemStack): Boolean = stack.has(ModDataComponents.ENTITY_DATA)

	companion object {
		private fun placeScoochworm(
			stack: ItemStack,
			level: Level,
			pos: BlockPos
		): ScoochwormEntity? {
			val entityData = stack.get(ModDataComponents.ENTITY_DATA) ?: return null
			val entity = ScoochwormEntity(ModEntityTypes.SCOOCHWORM.get(), level)
			entity.load(entityData.copyTag())
			entity.setPos(pos.bottomCenter)
			level.addFreshEntity(entity)
			return entity
		}
	}

}