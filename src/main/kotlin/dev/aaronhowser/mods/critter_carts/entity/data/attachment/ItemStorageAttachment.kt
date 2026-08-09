package dev.aaronhowser.mods.critter_carts.entity.data.attachment

import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import net.minecraft.core.component.DataComponents
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.SimpleContainer
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemContainerContents

class ItemStorageAttachment(
	saddlebag: ItemStack
) : ScoochwormAttachment(saddlebag) {

	override val type = ScoochwormAttachmentType.ITEM_STORAGE
	override val equipSound: SoundEvent = SoundEvents.DONKEY_CHEST

	private val container = SimpleContainer(CONTAINER_SIZE)

	init {
		val contents = saddlebag.getOrDefault(
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
		val menuProvider = SimpleMenuProvider(
			{ containerId, playerInventory, _ ->
				ChestMenu.threeRows(containerId, playerInventory, container)
			},
			itemStack.hoverName
		)

		player.openMenu(menuProvider)
		return AttachmentInteractionResult.Consume
	}

	override fun synchronizeItemStack() {
		updateItemContents()
	}

	private fun updateItemContents() {
		val contents = ItemContainerContents.fromItems(container.items)
		itemStack.set(DataComponents.CONTAINER, contents)
	}

	companion object {
		private const val CONTAINER_SIZE = 27
	}
}