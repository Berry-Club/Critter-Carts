package dev.aaronhowser.mods.critter_carts.menu.spider_nest

import dev.aaronhowser.mods.aaron.menu.BaseScreen
import dev.aaronhowser.mods.aaron.menu.textures.ScreenBackground
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.handler.web.spider.HoppingSpider
import dev.aaronhowser.mods.critter_carts.handler.web.spider.HoppingSpiderJob
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import java.util.Locale

class SpiderNestScreen(
	menu: SpiderNestMenu,
	playerInventory: Inventory,
	title: Component
) : BaseScreen<SpiderNestMenu>(menu, playerInventory, title) {

	override val background: ScreenBackground = BACKGROUND
	override val showInventoryLabel: Boolean = false

	override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
		super.renderLabels(guiGraphics, mouseX, mouseY)
		val nest = menu.getNest() ?: return
		val gameTime = nest.level?.gameTime ?: 0

		for ((index, spider) in nest.hoppingSpiders.withIndex()) {
			renderSpider(guiGraphics, spider, index, gameTime)
		}
	}

	private fun renderSpider(
		guiGraphics: GuiGraphics,
		spider: HoppingSpider,
		index: Int,
		gameTime: Long
	) {
		val rowY = FIRST_ROW_Y + index * ROW_HEIGHT
		val name = Component.translatable("menu.critter_carts.spider_nest.spider", index + 1)
		val position = getPositionText(spider, gameTime)
		val job = getJobText(spider)

		guiGraphics.drawString(font, name, TEXT_X, rowY, LABEL_COLOR, false)
		guiGraphics.drawString(font, position, TEXT_X, rowY + LINE_HEIGHT, TEXT_COLOR, false)
		guiGraphics.drawString(font, job, TEXT_X, rowY + LINE_HEIGHT * 2, TEXT_COLOR, false)
	}

	private fun getPositionText(spider: HoppingSpider, gameTime: Long): Component {
		val position = spider.getRenderPosition(gameTime, 0f)
			?: return Component.translatable("menu.critter_carts.spider_nest.position", "?", "?", "?")

		return Component.translatable(
			"menu.critter_carts.spider_nest.position",
			formatCoordinate(position.x),
			formatCoordinate(position.y),
			formatCoordinate(position.z)
		)
	}

	private fun formatCoordinate(coordinate: Double): String {
		return String.format(Locale.ROOT, "%.1f", coordinate)
	}

	private fun getJobText(spider: HoppingSpider): Component {
		val job = spider.job ?: return Component.translatable("menu.critter_carts.spider_nest.idle")

		return when (job.phase) {
			HoppingSpiderJob.Phase.TO_SOURCE -> Component.translatable(
				"menu.critter_carts.spider_nest.collecting",
				job.transferAmount
			)

			HoppingSpiderJob.Phase.TO_DESTINATION -> Component.translatable(
				"menu.critter_carts.spider_nest.delivering",
				spider.carriedStack.hoverName
			)

			HoppingSpiderJob.Phase.RETURNING ->
				Component.translatable("menu.critter_carts.spider_nest.returning")
		}
	}

	companion object {
		private const val TEXT_X = 8
		private const val FIRST_ROW_Y = 22
		private const val ROW_HEIGHT = 48
		private const val LINE_HEIGHT = 11
		private const val LABEL_COLOR = 0x404040
		private const val TEXT_COLOR = 0x606060

		val BACKGROUND = ScreenBackground(CritterCarts.modResource("textures/gui/spider_nest.png"), 176, 241)
	}
}