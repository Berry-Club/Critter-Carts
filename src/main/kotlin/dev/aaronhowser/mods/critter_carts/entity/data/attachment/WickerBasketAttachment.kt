package dev.aaronhowser.mods.critter_carts.entity.data.attachment

import dev.aaronhowser.mods.critter_carts.config.ServerConfig
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemContainerContents
import net.neoforged.neoforge.items.IItemHandler
import net.neoforged.neoforge.items.wrapper.InvWrapper

class WickerBasketAttachment(
	wickerBasket: ItemStack
) : ScoochwormAttachment(wickerBasket) {

	override val type = ScoochwormAttachmentType.WICKER_BASKET
	override val equipSound: SoundEvent = SoundEvents.DONKEY_CHEST

	private val container = SimpleContainer(CONTAINER_SIZE)
	override val itemHandler: IItemHandler = InvWrapper(container)
	private var upsideDownTicks = 0

	init {
		val contents = wickerBasket.getOrDefault(
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
	): AttachmentInteractionResult = AttachmentInteractionResult.Pass

	override fun synchronizeItemStack() {
		updateItemContents()
	}

	fun insert(itemStack: ItemStack): ItemStack {
		return container.addItem(itemStack)
	}

	override fun serverTick(bodyPart: ScoochwormPartEntity) {
		if (bodyPart.supportDirection != Direction.UP) {
			upsideDownTicks = 0
			return
		}

		upsideDownTicks++
		val dropInterval = ServerConfig.CONFIG.wickerBasketDropIntervalTicks.get()
		if (upsideDownTicks < dropInterval) return

		upsideDownTicks = 0
		dropNextStack(bodyPart)
	}

	private fun dropNextStack(bodyPart: ScoochwormPartEntity) {
		val maximumDropAmount = ServerConfig.CONFIG.wickerBasketDropAmount.get()

		for (slot in 0 until container.containerSize) {
			val itemStack = container.getItem(slot)
			if (itemStack.isEmpty) continue

			val amount = minOf(maximumDropAmount, itemStack.count)
			val droppedItem = container.removeItem(slot, amount)
			spawnDroppedItem(bodyPart, droppedItem)
			return
		}
	}

	private fun spawnDroppedItem(bodyPart: ScoochwormPartEntity, itemStack: ItemStack) {
		val itemEntity = ItemEntity(
			bodyPart.level(),
			bodyPart.x,
			bodyPart.y,
			bodyPart.z,
			itemStack
		)

		itemEntity.setDeltaMovement(0.0, 0.0, 0.0)
		itemEntity.setDefaultPickUpDelay()
		bodyPart.level().addFreshEntity(itemEntity)
	}

	private fun updateItemContents() {
		val contents = ItemContainerContents.fromItems(container.items)
		itemStack.set(DataComponents.CONTAINER, contents)
	}

	companion object {
		private const val CONTAINER_SIZE = 27
	}
}