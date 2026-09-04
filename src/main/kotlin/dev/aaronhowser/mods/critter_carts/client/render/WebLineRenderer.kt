package dev.aaronhowser.mods.critter_carts.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import dev.aaronhowser.mods.aaron.client.render.AaronRenderUtil
import dev.aaronhowser.mods.aaron.misc.AaronDsls.withPose
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isItem
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.toVec3
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.config.ClientConfig
import dev.aaronhowser.mods.critter_carts.handler.web.WebLineInteractionHandler
import dev.aaronhowser.mods.critter_carts.handler.web.line.ClientWebLines
import dev.aaronhowser.mods.critter_carts.handler.web.line.WebLine
import dev.aaronhowser.mods.critter_carts.handler.web.node.WebBlockAnchor
import dev.aaronhowser.mods.critter_carts.handler.web.node.WebNode
import dev.aaronhowser.mods.critter_carts.item.SpiderNestInterfaceItem
import dev.aaronhowser.mods.critter_carts.registry.ModDataComponents
import dev.aaronhowser.mods.critter_carts.registry.ModItems
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RenderLevelStageEvent
import org.joml.Quaternionf
import org.joml.Vector3f

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

		poseStack.withPose {
			for (line in ClientWebLines.getLines()) {
				val color = getWebLineColor(line)
				renderWebLine(
					poseStack,
					line.firstNode.position,
					line.secondNode.position,
					cameraPosition,
					color
				)
			}

			renderNestInterfaces(poseStack, cameraPosition)
			renderPlacementPreview(poseStack, cameraPosition, viewVector)
			renderLineAnchorPreview(poseStack, cameraPosition, viewVector)
		}
	}

	private fun renderNestInterfaces(poseStack: PoseStack, cameraPosition: Vec3) {
		val minecraft = Minecraft.getInstance()
		val level = minecraft.level ?: return
		val bufferSource = minecraft.renderBuffers().bufferSource()
		val vertexConsumer = bufferSource.getBuffer(WEB_RENDER_TYPE)

		for (node in ClientWebLines.getNodes()) {
			val anchor = node as? WebBlockAnchor ?: continue
			if (!anchor.hasNestInterface) continue

			val component = SpiderNestInterfaceItem.getComponent(anchor.nestInterface)
			val rotation = Quaternionf().rotationTo(
				0f,
				1f,
				0f,
				anchor.face.stepX.toFloat(),
				anchor.face.stepY.toFloat(),
				anchor.face.stepZ.toFloat()
			)
			val light = LevelRenderer.getLightColor(level, anchor.blockPos.relative(anchor.face))

			poseStack.withPose {
				val position = anchor.position.subtract(cameraPosition)
				poseStack.translate(position.x, position.y, position.z)
				poseStack.mulPose(rotation)
				poseStack.translate(0.0, INTERFACE_SURFACE_OFFSET, 0.0)

				val pose = poseStack.last()
				renderNestInterface(
					vertexConsumer,
					pose,
					light,
					component.color.textureDiffuseColor
				)
			}
		}

		bufferSource.endBatch(WEB_RENDER_TYPE)
	}

	private fun renderNestInterface(
		vertexConsumer: VertexConsumer,
		pose: PoseStack.Pose,
		light: Int,
		color: Int
	) {
		val bottomNorthWest = Vector3f(-INTERFACE_RADIUS, 0f, -INTERFACE_RADIUS)
		val bottomNorthEast = Vector3f(INTERFACE_RADIUS, 0f, -INTERFACE_RADIUS)
		val bottomSouthEast = Vector3f(INTERFACE_RADIUS, 0f, INTERFACE_RADIUS)
		val bottomSouthWest = Vector3f(-INTERFACE_RADIUS, 0f, INTERFACE_RADIUS)
		val topNorthWest = Vector3f(-INTERFACE_RADIUS, INTERFACE_HEIGHT, -INTERFACE_RADIUS)
		val topNorthEast = Vector3f(INTERFACE_RADIUS, INTERFACE_HEIGHT, -INTERFACE_RADIUS)
		val topSouthEast = Vector3f(INTERFACE_RADIUS, INTERFACE_HEIGHT, INTERFACE_RADIUS)
		val topSouthWest = Vector3f(-INTERFACE_RADIUS, INTERFACE_HEIGHT, INTERFACE_RADIUS)

		addInterfaceFace(
			vertexConsumer, pose,
			bottomNorthWest, bottomNorthEast, bottomSouthEast, bottomSouthWest,
			INTERFACE_TOP_TEXTURE_HEIGHT, light, color
		)
		addInterfaceFace(
			vertexConsumer, pose,
			topSouthWest, topSouthEast, topNorthEast, topNorthWest,
			INTERFACE_TOP_TEXTURE_HEIGHT, light, color
		)
		addInterfaceFace(
			vertexConsumer, pose,
			bottomNorthEast, bottomNorthWest, topNorthWest, topNorthEast,
			INTERFACE_SIDE_TEXTURE_HEIGHT, light, color
		)
		addInterfaceFace(
			vertexConsumer, pose,
			bottomSouthWest, bottomSouthEast, topSouthEast, topSouthWest,
			INTERFACE_SIDE_TEXTURE_HEIGHT, light, color
		)
		addInterfaceFace(
			vertexConsumer, pose,
			bottomNorthWest, bottomSouthWest, topSouthWest, topNorthWest,
			INTERFACE_SIDE_TEXTURE_HEIGHT, light, color
		)
		addInterfaceFace(
			vertexConsumer, pose,
			bottomSouthEast, bottomNorthEast, topNorthEast, topSouthEast,
			INTERFACE_SIDE_TEXTURE_HEIGHT, light, color
		)
	}

	private fun addInterfaceFace(
		vertexConsumer: VertexConsumer,
		pose: PoseStack.Pose,
		first: Vector3f,
		second: Vector3f,
		third: Vector3f,
		fourth: Vector3f,
		textureHeight: Float,
		light: Int,
		color: Int
	) {
		val normal = second.sub(first, Vector3f())
			.cross(fourth.sub(first, Vector3f()))
			.normalize()

		addVertex(vertexConsumer, pose, first.x, first.y, first.z, 0f, 0f, light, color, normal.x, normal.y, normal.z)
		addVertex(vertexConsumer, pose, second.x, second.y, second.z, 1f, 0f, light, color, normal.x, normal.y, normal.z)
		addVertex(vertexConsumer, pose, third.x, third.y, third.z, 1f, textureHeight, light, color, normal.x, normal.y, normal.z)
		addVertex(vertexConsumer, pose, fourth.x, fourth.y, fourth.z, 0f, textureHeight, light, color, normal.x, normal.y, normal.z)
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
			eyePosition,
			color
		)
	}

	private fun renderWebLine(
		poseStack: PoseStack,
		start: Vec3,
		end: Vec3,
		cameraPosition: Vec3,
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
			val relativeStart = start.subtract(cameraPosition)
			poseStack.translate(relativeStart.x, relativeStart.y, relativeStart.z)
			poseStack.mulPose(rotation)

			val pose = poseStack.last()
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

	private fun getWebLineColor(line: WebLine): Int {
		if (!ClientConfig.CONFIG.renderWebLineDebugColors.get()) return WEB_COLOR

		val hash = line.uuid.mostSignificantBits xor line.uuid.leastSignificantBits
		val hue = (hash and 0xFFFF).toFloat() / 0x10000
		val rgb = Mth.hsvToRgb(hue, 0.9f, 1f)

		return 0xFF000000.toInt() or rgb
	}

	private fun addSegment(
		vertexConsumer: VertexConsumer,
		pose: PoseStack.Pose,
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
			0f, 1f,
			start, end, 0f, 0.25f,
			startLight, endLight, color
		)

		addSide(
			vertexConsumer, pose,
			WEB_RADIUS, -WEB_RADIUS,
			WEB_RADIUS, WEB_RADIUS,
			-1f, 0f,
			start, end, 0.25f, 0.5f,
			startLight, endLight, color
		)

		addSide(
			vertexConsumer, pose,
			WEB_RADIUS, WEB_RADIUS,
			-WEB_RADIUS, WEB_RADIUS,
			0f, -1f,
			start, end, 0.5f, 0.75f,
			startLight, endLight, color
		)

		addSide(
			vertexConsumer, pose,
			-WEB_RADIUS, WEB_RADIUS,
			-WEB_RADIUS, -WEB_RADIUS,
			1f, 0f,
			start, end, 0.75f, 1f,
			startLight, endLight, color
		)
	}

	private fun addSide(
		vertexConsumer: VertexConsumer,
		pose: PoseStack.Pose,
		firstX: Float, firstZ: Float,
		secondX: Float, secondZ: Float,
		normalX: Float, normalZ: Float,
		start: Double, end: Double,
		minU: Float, maxU: Float,
		startLight: Int, endLight: Int,
		color: Int
	) {
		val minV = (start / TEXTURE_REPEAT_DISTANCE).toFloat()
		val maxV = (end / TEXTURE_REPEAT_DISTANCE).toFloat()

		addVertex(vertexConsumer, pose, firstX, start.toFloat(), firstZ, minU, minV, startLight, color, normalX, 0f, normalZ)
		addVertex(vertexConsumer, pose, secondX, start.toFloat(), secondZ, maxU, minV, startLight, color, normalX, 0f, normalZ)
		addVertex(vertexConsumer, pose, secondX, end.toFloat(), secondZ, maxU, maxV, endLight, color, normalX, 0f, normalZ)
		addVertex(vertexConsumer, pose, firstX, end.toFloat(), firstZ, minU, maxV, endLight, color, normalX, 0f, normalZ)
	}

	private fun addVertex(
		vertexConsumer: VertexConsumer,
		pose: PoseStack.Pose,
		x: Float, y: Float, z: Float,
		u: Float, v: Float,
		light: Int, color: Int,
		normalX: Float, normalY: Float, normalZ: Float
	) {
		vertexConsumer.addVertex(pose.pose(), x, y, z)
			.setColor(color)
			.setUv(u, v)
			.setOverlay(OverlayTexture.NO_OVERLAY)
			.setLight(light)
			.setNormal(pose, normalX, normalY, normalZ)
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
		val position = targetedNode.node.position.subtract(eyePosition)

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
	private const val INTERFACE_RADIUS = 2f / 16f
	private const val INTERFACE_HEIGHT = 2f / 16f
	private const val INTERFACE_SURFACE_OFFSET = 0.001
	private const val INTERFACE_TOP_TEXTURE_HEIGHT = 4f / 128f
	private const val INTERFACE_SIDE_TEXTURE_HEIGHT = 2f / 128f

	private val WEB_TEXTURE = CritterCarts.modResource("textures/misc/web_line.png")
	private val WEB_RENDER_TYPE = RenderType.entitySolid(WEB_TEXTURE)
}