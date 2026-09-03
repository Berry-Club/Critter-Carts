package dev.aaronhowser.mods.critter_carts.menu.nest_interface

import dev.aaronhowser.mods.aaron.menu.MenuWithButtons
import dev.aaronhowser.mods.aaron.menu.MenuWithInventory
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isItem
import dev.aaronhowser.mods.critter_carts.handler.web.WebSavedData
import dev.aaronhowser.mods.critter_carts.handler.web.node.WebBlockAnchor
import dev.aaronhowser.mods.critter_carts.item.SpiderNestInterfaceItem
import dev.aaronhowser.mods.critter_carts.item.component.NestInterfaceComponent
import dev.aaronhowser.mods.critter_carts.registry.ModItems
import dev.aaronhowser.mods.critter_carts.registry.ModMenuTypes
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.DataSlot
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.ItemStackHandler
import net.neoforged.neoforge.items.SlotItemHandler
import java.util.*

class NestInterfaceMenu private constructor(
	containerId: Int,
	playerInventory: Inventory,
	private val hand: InteractionHand?,
	private val anchorUuid: UUID?,
	private val clientAnchorStack: ItemStack
) : MenuWithInventory(ModMenuTypes.NEST_INTERFACE.get(), containerId, playerInventory), MenuWithButtons {

	constructor(containerId: Int, playerInventory: Inventory, hand: InteractionHand) :
		this(containerId, playerInventory, hand, null, ItemStack.EMPTY)

	constructor(containerId: Int, playerInventory: Inventory, anchor: WebBlockAnchor) :
		this(containerId, playerInventory, null, anchor.uuid, ItemStack.EMPTY)

	private val filterSlot = object : ItemStackHandler(1) {
		override fun isItemValid(slot: Int, stack: ItemStack): Boolean {
			return stack.isItem(ModItems.ITEM_FILTER)
		}

		override fun onContentsChanged(slot: Int) {
			SpiderNestInterfaceItem.setFilter(getInterfaceStack(), getStackInSlot(slot))
			syncAnchor()
		}
	}

	init {
		filterSlot.setStackInSlot(0, getComponent().getFilter())
		addDataSlot(object : DataSlot() {
			override fun get(): Int = getComponent().color.ordinal
			override fun set(value: Int) {
				SpiderNestInterfaceItem.setColor(getInterfaceStack(), DyeColor.entries[value])
			}
		})
		addDataSlot(object : DataSlot() {
			override fun get(): Int = getComponent().transferDirection.ordinal
			override fun set(value: Int) {
				val direction = NestInterfaceComponent.TransferDirection.entries[value]
				SpiderNestInterfaceItem.setTransferDirection(getInterfaceStack(), direction)
			}
		})
		addSlots(84)
	}

	private fun getInterfaceStack(): ItemStack {
		val hand = hand
		if (hand != null) return playerInventory.player.getItemInHand(hand)

		val level = playerInventory.player.level()
		if (level is ServerLevel) {
			return getAnchor(level)?.nestInterface ?: ItemStack.EMPTY
		}
		return clientAnchorStack
	}

	private fun getAnchor(level: ServerLevel): WebBlockAnchor? {
		val uuid = anchorUuid ?: return null
		return WebSavedData.get(level).getNode(uuid) as? WebBlockAnchor
	}

	private fun getComponent(): NestInterfaceComponent {
		return SpiderNestInterfaceItem.getComponent(getInterfaceStack())
	}

	private fun syncAnchor() {
		val level = playerInventory.player.level()
		if (level !is ServerLevel) return
		val anchor = getAnchor(level) ?: return
		WebSavedData.get(level).syncAnchor(level, anchor)
	}

	fun getColor(): DyeColor = getComponent().color
	fun isInput(): Boolean = getComponent().transferDirection == NestInterfaceComponent.TransferDirection.INPUT

	override fun addContainerSlots() {
		addSlot(SlotItemHandler(filterSlot, 0, 80, 35))
	}

	override fun stillValid(player: Player): Boolean {
		val hand = hand
		if (hand != null) return player.getItemInHand(hand).isItem(ModItems.SPIDER_NEST_INTERFACE)

		val level = player.level()
		if (level !is ServerLevel) return true
		val anchor = getAnchor(level) ?: return false
		return anchor.hasNestInterface && player.distanceToSqr(anchor.position) <= MAX_DISTANCE_SQUARED
	}

	override fun handleButtonPressed(buttonId: Int) {
		val stack = getInterfaceStack()
		when (buttonId) {
			CYCLE_COLOR_BUTTON_ID -> {
				val colors = DyeColor.entries
				val nextIndex = (getComponent().color.ordinal + 1) % colors.size
				SpiderNestInterfaceItem.setColor(stack, colors[nextIndex])
			}
			TOGGLE_DIRECTION_BUTTON_ID -> {
				val direction = getComponent().transferDirection.next()
				SpiderNestInterfaceItem.setTransferDirection(stack, direction)
			}
		}
		syncAnchor()
	}

	companion object {
		const val CYCLE_COLOR_BUTTON_ID = 0
		const val TOGGLE_DIRECTION_BUTTON_ID = 1
		private const val MAX_DISTANCE_SQUARED = 64.0

		fun fromNetwork(
			containerId: Int,
			playerInventory: Inventory,
			data: RegistryFriendlyByteBuf
		): NestInterfaceMenu {
			val targetsAnchor = data.readBoolean()
			if (!targetsAnchor) {
				return NestInterfaceMenu(
					containerId,
					playerInventory,
					data.readEnum(InteractionHand::class.java)
				)
			}

			val anchorUuid = data.readUUID()
			val interfaceStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(data)
			return NestInterfaceMenu(containerId, playerInventory, null, anchorUuid, interfaceStack)
		}
	}
}