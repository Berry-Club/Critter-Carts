package dev.aaronhowser.mods.critter_carts.client.render

import com.mojang.blaze3d.vertex.PoseStack
import dev.aaronhowser.mods.aaron.client.render.AaronRenderUtil
import dev.aaronhowser.mods.aaron.misc.AaronDsls.withPose
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isItem
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.toVec3
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.handler.web.line.ClientWebLines
import dev.aaronhowser.mods.critter_carts.handler.web.WebLineInteractionHandler
import dev.aaronhowser.mods.critter_carts.handler.web.node.WebNode
import dev.aaronhowser.mods.critter_carts.registry.ModDataComponents
import dev.aaronhowser.mods.critter_carts.registry.ModItems
import net.minecraft.client.Minecraft
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
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
		val viewVector = event.camera.lookVector.toVec3()
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

			renderPlacementPreview(poseStack, cameraPosition, viewVector)
			renderLineAnchorPreview(poseStack, cameraPosition, viewVector)
		}
	}

	private fun renderPlacementPreview(
		poseStack: PoseStack,
		eyePosition: Vec3,
		viewVector: Vec3
	) {
		val minecraft = Minecraft.getInstance()
		val player = minecraft.player ?: return
		val itemStack = getHeldWebFluid(player.mainHandItem, player.offhandItem) ?: return

		val firstNode = itemStack.get(ModDataComponents.WEB_NODE)?.node ?: return
		val secondNode = getTargetedNode(minecraft, eyePosition, viewVector) ?: return

		val isValid = WebLineInteractionHandler.canCreateLine(
			player.level(),
			player,
			firstNode,
			secondNode
		)
		val color = if (isValid) VALID_PREVIEW_COLOR else INVALID_PREVIEW_COLOR

		AaronRenderUtil.renderLineThroughWalls(
			poseStack,
			firstNode.position,
			secondNode.position,
			color
		)
	}

	private fun getHeldWebFluid(mainHandItem: ItemStack, offhandItem: ItemStack): ItemStack? {
		if (mainHandItem.isItem(ModItems.WEB_FLUID.get())) return mainHandItem
		if (offhandItem.isItem(ModItems.WEB_FLUID.get())) return offhandItem

		return null
	}

	private fun getTargetedNode(
		minecraft: Minecraft,
		eyePosition: Vec3,
		viewVector: Vec3
	): WebNode? {
		val player = minecraft.player ?: return null
		val targetedNode = ClientWebLines.getHoveredAnchor(player, eyePosition, viewVector)
		if (targetedNode != null) return targetedNode.node

		val hitResult = minecraft.hitResult
		if (hitResult !is BlockHitResult) return null
		if (hitResult.type != HitResult.Type.BLOCK) return null

		return WebLineInteractionHandler.createBlockAnchor(
			hitResult.blockPos,
			hitResult.direction,
			hitResult.location
		)
	}

	private fun renderLineAnchorPreview(
		poseStack: PoseStack,
		eyePosition: Vec3,
		viewVector: Vec3
	) {
		val minecraft = Minecraft.getInstance()
		val player = minecraft.player ?: return
		val targetedNode = ClientWebLines.getHoveredAnchor(
			player,
			eyePosition,
			viewVector
		) ?: return

		val cubeRadius = 0.05
		val anchorColor = 0xA0FFFFFF.toInt()
		val position = targetedNode.node.position

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
	private const val VALID_PREVIEW_COLOR = 0xFFFFFFFF.toInt()
	private const val INVALID_PREVIEW_COLOR = 0xFFFF0000.toInt()
}