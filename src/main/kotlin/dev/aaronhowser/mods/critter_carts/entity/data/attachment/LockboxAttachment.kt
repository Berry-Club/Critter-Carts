package dev.aaronhowser.mods.critter_carts.entity.data.attachment

import dev.aaronhowser.mods.critter_carts.config.ServerConfig
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.SimpleContainer
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.item.ItemEntity
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
	private var upsideDownTicks = 0
	private var previousShouldOpen: Boolean? = null

	var previousOpenProgress = 0f
		private set
	var openProgress = 0f
		private set

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

	override fun clientTick(bodyPart: ScoochwormPartEntity) {
		previousOpenProgress = openProgress

		val shouldOpen = bodyPart.isLockboxOpen
			|| bodyPart.supportDirection == Direction.UP

		val wasOpen = previousShouldOpen
		if (wasOpen != null && wasOpen != shouldOpen) {
			playOpenSound(bodyPart, shouldOpen)
		}
		previousShouldOpen = shouldOpen

		val change = if (shouldOpen) OPEN_SPEED else -OPEN_SPEED
		openProgress = (openProgress + change).coerceIn(0f, 1f)
	}

	override fun serverTick(bodyPart: ScoochwormPartEntity) {
		this.bodyPart = bodyPart
		updateOpenState()

		if (bodyPart.supportDirection != Direction.UP) {
			upsideDownTicks = 0
			return
		}

		upsideDownTicks++
		val dropInterval = ServerConfig.CONFIG.lockboxDropIntervalTicks.get()
		if (upsideDownTicks < dropInterval) return

		upsideDownTicks = 0
		dropNextStack(bodyPart)
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

	private fun playOpenSound(bodyPart: ScoochwormPartEntity, isOpening: Boolean) {
		val sound = if (isOpening) SoundEvents.CHEST_OPEN else SoundEvents.CHEST_CLOSE
		val pitch = bodyPart.random.nextFloat() * 0.1f + 0.9f

		bodyPart.level().playLocalSound(
			bodyPart.x,
			bodyPart.y,
			bodyPart.z,
			sound,
			SoundSource.BLOCKS,
			0.5f,
			pitch,
			false
		)
	}

	private fun dropNextStack(bodyPart: ScoochwormPartEntity) {
		val maximumDropAmount = ServerConfig.CONFIG.lockboxDropAmount.get()

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

	companion object {
		private const val CONTAINER_ROWS = 3
		private const val CONTAINER_SIZE = 27
		private const val OPEN_SPEED = 0.1f
	}
}