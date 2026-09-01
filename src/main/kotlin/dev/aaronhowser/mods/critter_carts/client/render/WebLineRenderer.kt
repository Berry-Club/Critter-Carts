package dev.aaronhowser.mods.critter_carts.client.render

import com.mojang.blaze3d.vertex.PoseStack
import dev.aaronhowser.mods.aaron.client.render.AaronRenderUtil
import dev.aaronhowser.mods.aaron.misc.AaronDsls.withPose
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.handler.web.ClientWebLines
import net.minecraft.client.Minecraft
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RenderLevelStageEvent

@EventBusSubscriber(
	modid = CritterCarts.MOD_ID,
	value = [Dist.CLIENT]
)
object WebLineRenderer {

	@SubscribeEvent
	fun renderWebLines(event: RenderLevelStageEvent) {
		if (event.stage != RenderLevelStageEvent.Stage.AFTER_WEATHER) return

		val cameraPosition = event.camera.position
		val poseStack = event.poseStack

		poseStack.withPose {
			poseStack.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z)

			for (line in ClientWebLines.getLines()) {
				AaronRenderUtil.renderLineThroughWalls(
					poseStack,
					line.firstNode.position,
					line.secondNode.position,
					WEB_COLOR
				)
			}

			renderLineAnchorPreview(poseStack)
		}
	}

	private fun renderLineAnchorPreview(poseStack: PoseStack) {
		val minecraft = Minecraft.getInstance()
		val player = minecraft.player ?: return
		val lineAnchor = ClientWebLines.getHoveredAnchor(player) ?: return
		val cubeRadius = 0.05
		val anchorColor = 0xA0FFFFFF.toInt()
		val position = lineAnchor.position

		AaronRenderUtil.renderCubeThroughWalls(
			poseStack,
			position.x - cubeRadius,
			position.y - cubeRadius,
			position.z - cubeRadius,
			position.x + cubeRadius,
			position.y + cubeRadius,
			position.z + cubeRadius,
			anchorColor
		)
	}

	private const val WEB_COLOR = 0xFFFFFFFF.toInt()
}