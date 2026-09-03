package dev.aaronhowser.mods.critter_carts.menu.nest_interface

import dev.aaronhowser.mods.aaron.menu.BaseScreen
import dev.aaronhowser.mods.aaron.menu.textures.ScreenBackground
import dev.aaronhowser.mods.aaron.packet.c2s.ClientClickedMenuButton
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.packet.client_to_server.SetNestInterfacePriorityPacket
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

class NestInterfaceScreen(menu: NestInterfaceMenu, inventory: Inventory, title: Component) :
	BaseScreen<NestInterfaceMenu>(menu, inventory, title) {

	override val background: ScreenBackground = BACKGROUND
	override val inventoryLabelOffsetY: Int = -2

	private lateinit var colorButton: Button
	private lateinit var directionButton: Button
	private lateinit var priorityInput: EditBox

	override fun baseInit() {
		super.baseInit()
		colorButton = Button.builder(getColorMessage()) {
			ClientClickedMenuButton(NestInterfaceMenu.CYCLE_COLOR_BUTTON_ID).messageServer()
		}.bounds(leftPos + 12, topPos + 57, 72, 20).build()
		directionButton = Button.builder(getDirectionMessage()) {
			ClientClickedMenuButton(NestInterfaceMenu.TOGGLE_DIRECTION_BUTTON_ID).messageServer()
		}.bounds(leftPos + 92, topPos + 57, 72, 20).build()
		priorityInput = EditBox(
			font,
			leftPos + 12,
			topPos + 32,
			56,
			18,
			Component.translatable("menu.critter_carts.interface.priority")
		)
		priorityInput.setMaxLength(11)
		priorityInput.setFilter { value -> value.isEmpty() || value == "-" || value.toIntOrNull() != null }
		priorityInput.value = menu.getPriority().toString()
		priorityInput.setResponder { value ->
			val priority = value.toIntOrNull() ?: return@setResponder
			SetNestInterfacePriorityPacket(priority).messageServer()
		}
		addRenderableWidget(colorButton)
		addRenderableWidget(directionButton)
		addRenderableWidget(priorityInput)
	}

	override fun containerTick() {
		super.containerTick()
		colorButton.message = getColorMessage()
		directionButton.message = getDirectionMessage()
	}

	private fun getColorMessage(): Component {
		return Component.translatable("menu.critter_carts.interface.color", menu.getColor().name)
	}

	private fun getDirectionMessage(): Component {
		val key = if (menu.isInput()) "menu.critter_carts.interface.input" else "menu.critter_carts.interface.output"
		return Component.translatable(key)
	}

	companion object {
		val BACKGROUND = ScreenBackground(CritterCarts.modResource("textures/gui/nest_interface.png"), 176, 166)
	}
}