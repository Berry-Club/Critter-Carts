package dev.aaronhowser.mods.critterworks.menu.spider_nest

import dev.aaronhowser.mods.critterworks.block_entity.HoppingSpiderNestBlockEntity
import dev.aaronhowser.mods.critterworks.registry.ModMenuTypes
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack

class SpiderNestMenu(
	containerId: Int,
	private val playerInventory: Inventory,
	val nestPos: BlockPos
) : AbstractContainerMenu(ModMenuTypes.SPIDER_NEST.get(), containerId) {

	constructor(
		containerId: Int,
		playerInventory: Inventory,
		nest: HoppingSpiderNestBlockEntity
	) : this(containerId, playerInventory, nest.blockPos)

	fun getNest(): HoppingSpiderNestBlockEntity? {
		val level = playerInventory.player.level()

		return level.getBlockEntity(nestPos) as? HoppingSpiderNestBlockEntity
	}

	override fun stillValid(player: Player): Boolean {
		if (getNest() == null) return false

		return player.distanceToSqr(nestPos.center) <= MAX_DISTANCE_SQUARED
	}

	override fun quickMoveStack(player: Player, index: Int): ItemStack = ItemStack.EMPTY

	companion object {
		private const val MAX_DISTANCE_SQUARED = 64.0

		fun fromNetwork(
			containerId: Int,
			playerInventory: Inventory,
			data: RegistryFriendlyByteBuf
		): SpiderNestMenu {
			return SpiderNestMenu(containerId, playerInventory, data.readBlockPos())
		}
	}
}