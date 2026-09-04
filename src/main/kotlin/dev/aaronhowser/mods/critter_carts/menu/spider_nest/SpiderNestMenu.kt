package dev.aaronhowser.mods.critter_carts.menu.spider_nest

import dev.aaronhowser.mods.critter_carts.block_entity.HoppingSpiderNestBlockEntity
import dev.aaronhowser.mods.critter_carts.handler.web.spider.HoppingSpiderJob
import dev.aaronhowser.mods.critter_carts.registry.ModMenuTypes
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.inventory.SimpleContainerData
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3

class SpiderNestMenu private constructor(
	containerId: Int,
	private val playerInventory: Inventory,
	val nestPos: BlockPos,
	private val data: ContainerData
) : AbstractContainerMenu(ModMenuTypes.SPIDER_NEST.get(), containerId) {

	constructor(
		containerId: Int,
		playerInventory: Inventory,
		nest: HoppingSpiderNestBlockEntity
	) : this(containerId, playerInventory, nest.blockPos, nest.menuData)

	init {
		checkContainerDataCount(data, SpiderNestData.DATA_COUNT)
		addDataSlots(data)
	}

	fun getPosition(spiderIndex: Int): Vec3? {
		if (!hasPosition(spiderIndex)) return null
		return Vec3(
			decodeCoordinate(data.get(SpiderNestData.getIndex(spiderIndex, SpiderNestData.POSITION_X_OFFSET))),
			decodeCoordinate(data.get(SpiderNestData.getIndex(spiderIndex, SpiderNestData.POSITION_Y_OFFSET))),
			decodeCoordinate(data.get(SpiderNestData.getIndex(spiderIndex, SpiderNestData.POSITION_Z_OFFSET)))
		)
	}

	private fun hasPosition(spiderIndex: Int): Boolean {
		return data.get(SpiderNestData.getIndex(spiderIndex, SpiderNestData.HAS_POSITION_OFFSET)) != 0
	}

	fun getPhase(spiderIndex: Int): HoppingSpiderJob.Phase? {
		val encodedPhase = data.get(SpiderNestData.getIndex(spiderIndex, SpiderNestData.PHASE_OFFSET))
		if (encodedPhase == 0) return null
		return HoppingSpiderJob.Phase.entries.getOrNull(encodedPhase - 1)
	}

	fun getTransferAmount(spiderIndex: Int): Int {
		return data.get(SpiderNestData.getIndex(spiderIndex, SpiderNestData.TRANSFER_AMOUNT_OFFSET))
	}

	fun getCarriedStack(spiderIndex: Int): ItemStack {
		val itemId = data.get(SpiderNestData.getIndex(spiderIndex, SpiderNestData.ITEM_ID_OFFSET))
		val count = data.get(SpiderNestData.getIndex(spiderIndex, SpiderNestData.ITEM_COUNT_OFFSET))
		if (count == 0) return ItemStack.EMPTY
		return ItemStack(BuiltInRegistries.ITEM.byId(itemId), count)
	}

	override fun stillValid(player: Player): Boolean {
		val nest = playerInventory.player.level().getBlockEntity(nestPos)
		if (nest !is HoppingSpiderNestBlockEntity) return false
		return player.distanceToSqr(nestPos.center) <= MAX_DISTANCE_SQUARED
	}

	override fun quickMoveStack(player: Player, index: Int): ItemStack = ItemStack.EMPTY

	companion object {
		const val SPIDER_COUNT = 4
		private const val COORDINATE_SCALE = 10.0
		private const val MAX_DISTANCE_SQUARED = 64.0

		private fun decodeCoordinate(coordinate: Int): Double {
			return coordinate / COORDINATE_SCALE
		}

		fun fromNetwork(
			containerId: Int,
			playerInventory: Inventory,
			data: RegistryFriendlyByteBuf
		): SpiderNestMenu {
			return SpiderNestMenu(
				containerId,
				playerInventory,
				data.readBlockPos(),
				SimpleContainerData(SpiderNestData.DATA_COUNT)
			)
		}
	}
}