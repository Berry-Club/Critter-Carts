package dev.aaronhowser.mods.critter_carts.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import dev.aaronhowser.mods.critter_carts.client.model.ScoochwormPartModel
import dev.aaronhowser.mods.critter_carts.client.renderer.layer.ScoochwormSaddleLayer
import dev.aaronhowser.mods.critter_carts.client.renderer.layer.ScoochwormSaddlebagLayer
import dev.aaronhowser.mods.critter_carts.client.renderer.layer.ScoochwormWickerBasketLayer
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import software.bernie.geckolib.renderer.GeoEntityRenderer

class ScoochwormPartRenderer(
	context: EntityRendererProvider.Context
) : GeoEntityRenderer<ScoochwormPartEntity>(context, ScoochwormPartModel()) {

	init {
		withScale(ScoochwormEntity.SIZE)
		addRenderLayer(ScoochwormSaddlebagLayer(this))
		addRenderLayer(ScoochwormWickerBasketLayer(this))
		addRenderLayer(ScoochwormSaddleLayer(this))
	}

	override fun getTextureLocation(animatable: ScoochwormPartEntity): ResourceLocation {
		return animatable.color.bodyTexture
	}

	override fun applyRotations(
		animatable: ScoochwormPartEntity,
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

		ScoochwormRenderer.applyRotations(
			poseStack,
			animatable.supportDirection,
			interpolatedYaw
		)
	}

}