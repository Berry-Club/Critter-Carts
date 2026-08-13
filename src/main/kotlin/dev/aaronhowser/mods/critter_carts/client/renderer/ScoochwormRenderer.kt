package dev.aaronhowser.mods.critter_carts.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.client.model.ScoochwormModel
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import org.joml.Vector3f
import software.bernie.geckolib.renderer.GeoEntityRenderer

class ScoochwormRenderer(
	context: EntityRendererProvider.Context
) : GeoEntityRenderer<ScoochwormEntity>(context, ScoochwormModel()) {

	init {
		withScale(ScoochwormEntity.SIZE)
	}

	override fun getTextureLocation(animatable: ScoochwormEntity): ResourceLocation = TEXTURE

	override fun applyRotations(
		animatable: ScoochwormEntity,
		poseStack: PoseStack,
		ageInTicks: Float,
		rotationYaw: Float,
		partialTick: Float,
		nativeScale: Float
	) {
		applyRotations(
			poseStack,
			animatable.previousSupportDirection,
			animatable.previousForwardDirection,
			animatable.supportDirection,
			animatable.forwardDirection,
			partialTick
		)
	}

	companion object {
		val TEXTURE: ResourceLocation = CritterCarts.modResource("textures/entity/scoochworm/head.png")
		private const val ROTATION_CENTER = ScoochwormEntity.SIZE / 2.0

		fun applyRotations(
			poseStack: PoseStack,
			previousSupportDirection: Direction,
			previousForwardDirection: Vec3,
			supportDirection: Direction,
			forwardDirection: Vec3,
			partialTick: Float
		) {
			val previousRotation = getRotation(
				previousSupportDirection,
				previousForwardDirection
			)
			val currentRotation = getRotation(supportDirection, forwardDirection)
			val interpolatedRotation = previousRotation.slerp(
				currentRotation,
				partialTick,
				Quaternionf()
			)

			poseStack.translate(0.0, ROTATION_CENTER, 0.0)
			poseStack.mulPose(interpolatedRotation)
			poseStack.translate(0.0, -ROTATION_CENTER, 0.0)
		}

		private fun getRotation(
			supportDirection: Direction,
			forwardDirection: Vec3
		): Quaternionf {
			val up = Vector3f(
				-supportDirection.stepX.toFloat(),
				-supportDirection.stepY.toFloat(),
				-supportDirection.stepZ.toFloat()
			)
			val forward = Vector3f(
				forwardDirection.x.toFloat(),
				forwardDirection.y.toFloat(),
				forwardDirection.z.toFloat()
			)

			forward.sub(Vector3f(up).mul(forward.dot(up)))
			if (forward.lengthSquared() == 0f) {
				forward.set(0f, 0f, 1f)
				forward.sub(Vector3f(up).mul(forward.dot(up)))
			}
			if (forward.lengthSquared() == 0f) {
				forward.set(0f, 1f, 0f)
				forward.sub(Vector3f(up).mul(forward.dot(up)))
			}

			return Quaternionf().lookAlong(forward.normalize(), up)
		}

	}
}