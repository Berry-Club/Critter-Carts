package dev.aaronhowser.mods.critter_carts.menu

import dev.aaronhowser.mods.aaron.menu.textures.ScreenSprite
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.Component

class ToggleSpriteButton(
	x: Int,
	y: Int,
	width: Int,
	height: Int,
	val font: Font,
	val sprites: Pair<ScreenSprite, ScreenSprite>,
	val messages: Pair<Component, Component>,
	val isOnGetter: () -> Boolean,
	onPress: OnPress
) : Button(x, y, width, height, messages.first, onPress, DEFAULT_NARRATION) {

	override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
		super.renderWidget(guiGraphics, mouseX, mouseY, partialTick)
		val sprite = if (isOnGetter()) sprites.first else sprites.second
		val spriteX = x + width / 2 - sprite.width / 2
		val spriteY = y + height / 2 - sprite.height / 2
		guiGraphics.blitSprite(sprite.texture, spriteX, spriteY, sprite.width, sprite.height)
		if (isHovered) {
			val message = if (isOnGetter()) messages.first else messages.second
			guiGraphics.renderTooltip(font, message, mouseX, mouseY)
		}
	}

	override fun renderString(guiGraphics: GuiGraphics, font: Font, color: Int) {}
}