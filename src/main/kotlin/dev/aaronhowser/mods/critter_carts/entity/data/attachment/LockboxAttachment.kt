package dev.aaronhowser.mods.critter_carts.entity.data.attachment

import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import net.minecraft.core.component.DataComponents
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.SimpleContainer
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemContainerContents
import net.neoforged.neoforge.items.IItemHandler
import net.neoforged.neoforge.items.wrapper.InvWrapper

class LockboxAttachment(
	lockbox: ItemStack
) : ScoochwormAttachment(lockbox) {

	override val type = ScoochwormAttachmentType.LOCKBOX
	override val equipSound: SoundEvent = SoundEvents.DONKEY_CHEST

	private var bodyPart: ScoochwormPartEntity? = null
	private var openers = 0

	private val container = object : SimpleContainer(CONTAINER_SIZE) {
		override fun startOpen(player: Player) {
			super.startOpen(player)
			openers++
			updateOpenState()
		}

		override fun stopOpen(player: Player) {
			super.stopOpen(player)
			if (openers > 0) openers--
			updateOpenState()
		}
	}

	override val itemHandler: IItemHandler = InvWrapper(container)

	init {
		val contents = lockbox.getOrDefault(
			DataComponents.CONTAINER,
			ItemContainerContents.EMPTY
		)
		contents.copyInto(container.items)

		container.addListener {
			updateItemContents()
		}
	}

	override fun interact(
		player: Player,
		heldStack: ItemStack,
		bodyPart: ScoochwormPartEntity
	): AttachmentInteractionResult {
		this.bodyPart = bodyPart

		val menuProvider = SimpleMenuProvider(
			{ containerId, playerInventory, _ ->
				ChestMenu(
					MenuType.GENERIC_9x3,
					containerId,
					playerInventory,
					container,
					CONTAINER_ROWS
				)
			},
			itemStack.hoverName
		)

		player.openMenu(menuProvider)
		return AttachmentInteractionResult.Consume
	}

	override fun synchronizeItemStack() {
		updateItemContents()
	}

	override fun serverTick(bodyPart: ScoochwormPartEntity) {
		this.bodyPart = bodyPart
		updateOpenState()
	}

	fun insert(itemStack: ItemStack): ItemStack {
		return container.addItem(itemStack)
	}

	private fun updateItemContents() {
		val contents = ItemContainerContents.fromItems(container.items)
		itemStack.set(DataComponents.CONTAINER, contents)
	}

	private fun updateOpenState() {
		bodyPart?.isLockboxOpen = openers > 0
	}

	companion object {
		private const val CONTAINER_ROWS = 3
		private const val CONTAINER_SIZE = 27
	}
}