package dev.aaronhowser.mods.critter_carts.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
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
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RenderLevelStageEvent
import org.joml.Matrix4f
import org.joml.Quaternionf

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
				renderWebLine(
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

		renderWebLine(
			poseStack,
			firstNode.position,
			secondNode.position,
			color
		)
	}

	private fun renderWebLine(
		poseStack: PoseStack,
		start: Vec3,
		end: Vec3,
		color: Int
	) {
		val offset = end.subtract(start)
		val height = offset.length()
		if (height == 0.0) return

		val minecraft = Minecraft.getInstance()
		val bufferSource = minecraft.renderBuffers().bufferSource()
		val vertexConsumer = bufferSource.getBuffer(WEB_RENDER_TYPE)
		val direction = offset.scale(1.0 / height)
		val rotation = Quaternionf().rotationTo(
			0f,
			1f,
			0f,
			direction.x.toFloat(),
			direction.y.toFloat(),
			direction.z.toFloat()
		)

		poseStack.withPose {
			poseStack.translate(start.x, start.y, start.z)
			poseStack.mulPose(rotation)

			val pose = poseStack.last().pose()
			val level = minecraft.level ?: return@withPose
			var segmentStart = 0.0
			var startLight = LevelRenderer.getLightColor(
				level,
				BlockPos.containing(start)
			)

			while (segmentStart < height) {
				val segmentEnd = minOf(segmentStart + LIGHT_SAMPLE_DISTANCE, height)
				val endPosition = start.add(direction.scale(segmentEnd))
				val endLight = LevelRenderer.getLightColor(
					level,
					BlockPos.containing(endPosition)
				)

				addSegment(
					vertexConsumer,
					pose,
					segmentStart,
					segmentEnd,
					startLight,
					endLight,
					color
				)

				segmentStart = segmentEnd
				startLight = endLight
			}
		}

		bufferSource.endBatch(WEB_RENDER_TYPE)
	}

	private fun addSegment(
		vertexConsumer: VertexConsumer,
		pose: Matrix4f,
		start: Double,
		end: Double,
		startLight: Int,
		endLight: Int,
		color: Int
	) {
		addSide(
			vertexConsumer, pose,
			-WEB_RADIUS, -WEB_RADIUS,
			WEB_RADIUS, -WEB_RADIUS,
			start, end, 0f, 0.25f,
			startLight, endLight, color
		)
		addSide(
			vertexConsumer, pose,
			WEB_RADIUS, -WEB_RADIUS,
			WEB_RADIUS, WEB_RADIUS,
			start, end, 0.25f, 0.5f,
			startLight, endLight, color
		)
		addSide(
			vertexConsumer, pose,
			WEB_RADIUS, WEB_RADIUS,
			-WEB_RADIUS, WEB_RADIUS,
			start, end, 0.5f, 0.75f,
			startLight, endLight, color
		)
		addSide(
			vertexConsumer, pose,
			-WEB_RADIUS, WEB_RADIUS,
			-WEB_RADIUS, -WEB_RADIUS,
			start, end, 0.75f, 1f,
			startLight, endLight, color
		)
	}

	private fun addSide(
		vertexConsumer: VertexConsumer,
		pose: Matrix4f,
		firstX: Float,
		firstZ: Float,
		secondX: Float,
		secondZ: Float,
		start: Double,
		end: Double,
		minU: Float,
		maxU: Float,
		startLight: Int,
		endLight: Int,
		color: Int
	) {
		val minV = (start / TEXTURE_REPEAT_DISTANCE).toFloat()
		val maxV = (end / TEXTURE_REPEAT_DISTANCE).toFloat()

		addVertex(vertexConsumer, pose, firstX, start.toFloat(), firstZ, minU, minV, startLight, color)
		addVertex(vertexConsumer, pose, secondX, start.toFloat(), secondZ, maxU, minV, startLight, color)
		addVertex(vertexConsumer, pose, secondX, end.toFloat(), secondZ, maxU, maxV, endLight, color)
		addVertex(vertexConsumer, pose, firstX, end.toFloat(), firstZ, minU, maxV, endLight, color)
	}

	private fun addVertex(
		vertexConsumer: VertexConsumer,
		pose: Matrix4f,
		x: Float,
		y: Float,
		z: Float,
		u: Float,
		v: Float,
		light: Int,
		color: Int
	) {
		vertexConsumer.addVertex(pose, x, y, z)
			.setColor(color)
			.setUv(u, v)
			.setOverlay(OverlayTexture.NO_OVERLAY)
			.setLight(light)
			.setNormal(0f, 1f, 0f)
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
	private const val WEB_RADIUS = 1f / 64f
	private const val TEXTURE_REPEAT_DISTANCE = 8.0
	private const val LIGHT_SAMPLE_DISTANCE = 1.0

	private val WEB_TEXTURE = ResourceLocation.fromNamespaceAndPath(
		CritterCarts.MOD_ID,
		"textures/misc/web_line.png"
	)
	private val WEB_RENDER_TYPE = RenderType.entityCutoutNoCull(WEB_TEXTURE)
}