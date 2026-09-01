package dev.aaronhowser.mods.critter_carts.client.render

import dev.aaronhowser.mods.aaron.client.render.AaronRenderUtil
import dev.aaronhowser.mods.aaron.misc.AaronDsls.withPose
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.handler.web.ClientWebLines
import dev.aaronhowser.mods.critter_carts.handler.web.WebLine
import dev.aaronhowser.mods.critter_carts.handler.web.WebNode
import net.minecraft.world.phys.Vec3
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
				val firstPosition = getNodePosition(line.firstNode) ?: continue
				val secondPosition = getNodePosition(line.secondNode) ?: continue

				AaronRenderUtil.renderLineThroughWalls(
					poseStack,
					firstPosition,
					secondPosition,
					WEB_COLOR
				)
			}
		}
	}

	private fun getNodePosition(node: WebNode): Vec3? {
		return when (node) {
			is WebNode.BlockAnchor -> {
				val blockCenter = Vec3.atCenterOf(node.blockPos)
				val faceOffset = Vec3.atLowerCornerOf(node.face.normal)
					.scale(0.5 + SURFACE_OFFSET)
				blockCenter.add(faceOffset)
			}

			is WebNode.LineAnchor -> {
				val line = ClientWebLines.getLine(node.lineUuid) ?: return null
				getPositionAlong(line, node.percentAlong)
			}
		}
	}

	private fun getPositionAlong(line: WebLine, percentAlong: Double): Vec3? {
		val firstPosition = getNodePosition(line.firstNode) ?: return null
		val secondPosition = getNodePosition(line.secondNode) ?: return null
		return firstPosition.lerp(secondPosition, percentAlong)
	}

	private const val SURFACE_OFFSET = 0.001
	private const val WEB_COLOR = 0xFFFFFFFF.toInt()
}