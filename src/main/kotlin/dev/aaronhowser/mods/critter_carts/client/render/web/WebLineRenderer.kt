package dev.aaronhowser.mods.critter_carts.client.render.web

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.toVec3
import dev.aaronhowser.mods.critter_carts.CritterCarts
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
		if (event.stage != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) return

		val cameraPosition = event.camera.position
		val viewVector = event.camera.lookVector.toVec3()
		val poseStack = event.poseStack

		WebLineGeometryRenderer.renderAll(poseStack, cameraPosition)
		NestInterfaceRenderer.renderAll(poseStack, cameraPosition)
		WebLinePreviewRenderer.render(poseStack, cameraPosition, viewVector)
	}
}