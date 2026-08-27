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

class SaddlebagAttachment(
	saddlebag: ItemStack
) : ScoochwormAttachment(saddlebag) {

	override val type = ScoochwormAttachmentType.SADDLEBAGS
	override val equipSound: SoundEvent = SoundEvents.DONKEY_CHEST

	private val container = SimpleContainer(CONTAINER_SIZE)
	override val itemHandler: IItemHandler = InvWrapper(container)

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
				ChestMenu(
					MenuType.GENERIC_9x1,
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

	private fun updateItemContents() {
		val contents = ItemContainerContents.fromItems(container.items)
		itemStack.set(DataComponents.CONTAINER, contents)
	}

	companion object {
		private const val CONTAINER_ROWS = 1
		private const val CONTAINER_SIZE = 9
	}
}