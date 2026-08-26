package dev.aaronhowser.mods.critter_carts.block

import dev.aaronhowser.mods.aaron.block_entity.SyncingBlockEntity
import dev.aaronhowser.mods.critter_carts.item.CritterCageItem
import dev.aaronhowser.mods.critter_carts.registry.ModBlockEntityTypes
import dev.aaronhowser.mods.critter_carts.registry.ModDataComponents
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.block.state.BlockState

class CritterCageBlockEntity(
	pos: BlockPos,
	state: BlockState
) : SyncingBlockEntity(ModBlockEntityTypes.CRITTER_CAGE.get(), pos, state) {

	override val syncImmediately: Boolean = true

	var entityData: CustomData? = null
		set(value) {
			field = value
			setChanged()
			level?.sendBlockUpdated(blockPos, blockState, blockState, 3)
		}

	val hasEntity: Boolean
		get() = entityData != null

	fun tryRelease(player: Player?): Boolean {
		val level = level ?: return false
		val data = entityData ?: return false
		val forward = blockState.getValue(CritterCageBlock.FORWARD)
		val spawnPos = blockPos.relative(forward)
		val stack = ItemStack(blockState.block)
		stack.set(ModDataComponents.ENTITY_DATA, data)

		val worm = CritterCageItem.placeScoochworm(
			stack,
			level,
			spawnPos,
			blockPos,
			forward,
			player
		) ?: return false

		if (!level.noCollision(worm)) {
			worm.discard()
			return false
		}

		entityData = null
		return true
	}

	fun copyEntityDataTo(stack: ItemStack) {
		val data = entityData ?: return
		stack.set(ModDataComponents.ENTITY_DATA, data)
	}

	override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
		super.saveAdditional(tag, registries)
		val data = entityData ?: return
		tag.put(ENTITY_DATA_TAG, data.copyTag())
	}

	override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
		super.loadAdditional(tag, registries)
		entityData = if (tag.contains(ENTITY_DATA_TAG)) {
			CustomData.of(tag.getCompound(ENTITY_DATA_TAG))
		} else {
			null
		}
	}

	companion object {
		private const val ENTITY_DATA_TAG = "EntityData"
	}
}