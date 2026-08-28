package dev.aaronhowser.mods.critter_carts.block_entity

import dev.aaronhowser.mods.aaron.block_entity.SyncingBlockEntity
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.getMinimalTag
import dev.aaronhowser.mods.critter_carts.block.CritterCageBlock
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import dev.aaronhowser.mods.critter_carts.item.CritterCageItem
import dev.aaronhowser.mods.critter_carts.registry.ModBlockEntityTypes
import dev.aaronhowser.mods.critter_carts.registry.ModDataComponents
import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

class CritterCageBlockEntity(
	pos: BlockPos,
	state: BlockState
) : SyncingBlockEntity(ModBlockEntityTypes.CRITTER_CAGE.get(), pos, state) {

	override val syncImmediately: Boolean = true
	private var cachedEntityData: CustomData? = null
	private var cachedEntityLevel: Level? = null
	private var cachedScoochworm: ScoochwormEntity? = null

	var entityData: CustomData? = null
		set(value) {
			field = value
			cachedEntityData = null
			cachedEntityLevel = null
			cachedScoochworm = null
			setChanged()
			level?.sendBlockUpdated(blockPos, blockState, blockState, 3)
		}

	val hasEntity: Boolean
		get() = entityData != null

	fun getScoochworm(): ScoochwormEntity? {
		val data = entityData ?: return null
		val level = level ?: return null

		if (data == cachedEntityData && level === cachedEntityLevel) {
			return cachedScoochworm
		}

		val scoochworm = ScoochwormEntity(ModEntityTypes.SCOOCHWORM.get(), level)
		scoochworm.load(data.copyTag())

		cachedEntityData = data
		cachedEntityLevel = level
		cachedScoochworm = scoochworm

		return scoochworm
	}

	fun tryCapture(scoochworm: ScoochwormEntity): Boolean {
		val level = level ?: return false
		if (level.isClientSide || hasEntity || !blockState.getValue(CritterCageBlock.OPEN)) return false

		val wormTag = scoochworm.getMinimalTag(stripUniqueness = false)
		wormTag.remove(ScoochwormEntity.PATH_TAG)
		entityData = CustomData.of(wormTag)
		level.setBlock(
			blockPos,
			blockState.setValue(CritterCageBlock.OPEN, false),
			Block.UPDATE_CLIENTS
		)
		scoochworm.discard()

		return true
	}

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
		level.setBlock(
			blockPos,
			blockState.setValue(CritterCageBlock.OPEN, true),
			Block.UPDATE_CLIENTS
		)
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