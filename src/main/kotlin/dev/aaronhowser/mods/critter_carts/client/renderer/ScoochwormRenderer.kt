package dev.aaronhowser.mods.critter_carts.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.client.model.ScoochwormModel
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
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
		val interpolatedYaw = Mth.rotLerp(
			partialTick,
			animatable.yRotO,
			animatable.yRot
		)

		applyRotations(
			poseStack,
			animatable.attachmentBottom,
			interpolatedYaw
		)
	}

	companion object {
		val TEXTURE: ResourceLocation = CritterCarts.modResource("textures/entity/scoochworm/head.png")

		fun applyRotations(
			poseStack: PoseStack,
			bottom: Direction,
			yaw: Float
		) {
			when (bottom) {
				Direction.DOWN -> Unit
				Direction.UP -> poseStack.mulPose(Axis.ZP.rotationDegrees(180f))
				Direction.NORTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90f))
				Direction.SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(-90f))
				Direction.WEST -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90f))
				Direction.EAST -> poseStack.mulPose(Axis.ZP.rotationDegrees(90f))
			}

			val surfaceYaw = if (bottom == Direction.UP) -yaw else yaw
			poseStack.mulPose(Axis.YP.rotationDegrees(180f - surfaceYaw))
		}
	}
}