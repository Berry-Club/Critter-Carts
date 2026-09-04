package dev.aaronhowser.mods.critterworks.client.render.web

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import dev.aaronhowser.mods.aaron.misc.AaronDsls.withPose
import dev.aaronhowser.mods.critterworks.Critterworks
import dev.aaronhowser.mods.critterworks.handler.web.line.ClientWebLines
import dev.aaronhowser.mods.critterworks.handler.web.node.WebBlockAnchor
import dev.aaronhowser.mods.critterworks.item.SpiderNestInterfaceItem
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import org.joml.Vector3f

object NestInterfaceRenderer {

	fun renderAll(poseStack: PoseStack, cameraPosition: Vec3) {
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

				render(vertexConsumer, poseStack.last(), light, component.color.textureDiffuseColor)
			}
		}

		bufferSource.endBatch(WEB_RENDER_TYPE)
	}

	private fun render(vertexConsumer: VertexConsumer, pose: PoseStack.Pose, light: Int, color: Int) {
		val bottomNorthWest = Vector3f(-INTERFACE_RADIUS, 0f, -INTERFACE_RADIUS)
		val bottomNorthEast = Vector3f(INTERFACE_RADIUS, 0f, -INTERFACE_RADIUS)
		val bottomSouthEast = Vector3f(INTERFACE_RADIUS, 0f, INTERFACE_RADIUS)
		val bottomSouthWest = Vector3f(-INTERFACE_RADIUS, 0f, INTERFACE_RADIUS)
		val topNorthWest = Vector3f(-INTERFACE_RADIUS, INTERFACE_HEIGHT, -INTERFACE_RADIUS)
		val topNorthEast = Vector3f(INTERFACE_RADIUS, INTERFACE_HEIGHT, -INTERFACE_RADIUS)
		val topSouthEast = Vector3f(INTERFACE_RADIUS, INTERFACE_HEIGHT, INTERFACE_RADIUS)
		val topSouthWest = Vector3f(-INTERFACE_RADIUS, INTERFACE_HEIGHT, INTERFACE_RADIUS)

		addFace(vertexConsumer, pose, bottomNorthWest, bottomNorthEast, bottomSouthEast, bottomSouthWest, INTERFACE_TOP_TEXTURE_HEIGHT, light, color)
		addFace(vertexConsumer, pose, topSouthWest, topSouthEast, topNorthEast, topNorthWest, INTERFACE_TOP_TEXTURE_HEIGHT, light, color)
		addFace(vertexConsumer, pose, bottomNorthEast, bottomNorthWest, topNorthWest, topNorthEast, INTERFACE_SIDE_TEXTURE_HEIGHT, light, color)
		addFace(vertexConsumer, pose, bottomSouthWest, bottomSouthEast, topSouthEast, topSouthWest, INTERFACE_SIDE_TEXTURE_HEIGHT, light, color)
		addFace(vertexConsumer, pose, bottomNorthWest, bottomSouthWest, topSouthWest, topNorthWest, INTERFACE_SIDE_TEXTURE_HEIGHT, light, color)
		addFace(vertexConsumer, pose, bottomSouthEast, bottomNorthEast, topNorthEast, topSouthEast, INTERFACE_SIDE_TEXTURE_HEIGHT, light, color)
	}

	private fun addFace(
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

		addVertex(vertexConsumer, pose, first, 0f, 0f, light, color, normal)
		addVertex(vertexConsumer, pose, second, 1f, 0f, light, color, normal)
		addVertex(vertexConsumer, pose, third, 1f, textureHeight, light, color, normal)
		addVertex(vertexConsumer, pose, fourth, 0f, textureHeight, light, color, normal)
	}

	private fun addVertex(
		vertexConsumer: VertexConsumer,
		pose: PoseStack.Pose,
		position: Vector3f,
		u: Float,
		v: Float,
		light: Int,
		color: Int,
		normal: Vector3f
	) {
		vertexConsumer.addVertex(pose.pose(), position.x, position.y, position.z)
			.setColor(color)
			.setUv(u, v)
			.setOverlay(OverlayTexture.NO_OVERLAY)
			.setLight(light)
			.setNormal(pose, normal.x, normal.y, normal.z)
	}

	private const val INTERFACE_RADIUS = 2f / 16f
	private const val INTERFACE_HEIGHT = 2f / 16f
	private const val INTERFACE_SURFACE_OFFSET = 0.001
	private const val INTERFACE_TOP_TEXTURE_HEIGHT = 4f / 128f
	private const val INTERFACE_SIDE_TEXTURE_HEIGHT = 2f / 128f

	private val WEB_TEXTURE = Critterworks.modResource("textures/misc/web_line.png")
	private val WEB_RENDER_TYPE = RenderType.entitySolid(WEB_TEXTURE)
}