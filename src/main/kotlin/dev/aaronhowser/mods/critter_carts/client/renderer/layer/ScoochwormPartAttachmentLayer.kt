package dev.aaronhowser.mods.critter_carts.client.renderer.layer

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import dev.aaronhowser.mods.aaron.misc.AaronDsls.withPose
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import dev.aaronhowser.mods.critter_carts.entity.data.ScoochwormPartAttachment
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import software.bernie.geckolib.cache.`object`.BakedGeoModel
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoRenderer
import software.bernie.geckolib.renderer.layer.GeoRenderLayer

abstract class ScoochwormPartAttachmentLayer(
	renderer: GeoRenderer<ScoochwormPartEntity>,
	private val attachment: ScoochwormPartAttachment,
	private val attachmentModel: GeoModel<ScoochwormPartEntity>
) : GeoRenderLayer<ScoochwormPartEntity>(renderer) {

	override fun getGeoModel(): GeoModel<ScoochwormPartEntity> = attachmentModel

	override fun render(
		poseStack: PoseStack,
		animatable: ScoochwormPartEntity,
		bakedModel: BakedGeoModel,
		renderType: RenderType?,
		bufferSource: MultiBufferSource,
		buffer: VertexConsumer?,
		partialTick: Float,
		packedLight: Int,
		packedOverlay: Int
	) {
		if (animatable.attachment != attachment) return

		val texture = attachmentModel.getTextureResource(animatable, renderer)
		val attachmentRenderType = renderer.getRenderType(
			animatable,
			texture,
			bufferSource,
			partialTick
		) ?: return

		poseStack.withPose {
			poseStack.translate(0.0, ATTACHMENT_OFFSET, 0.0)

			renderer.reRender(
				getDefaultBakedModel(animatable),
				poseStack,
				bufferSource,
				animatable,
				attachmentRenderType,
				bufferSource.getBuffer(attachmentRenderType),
				partialTick,
				packedLight,
				packedOverlay,
				renderer.getRenderColor(animatable, partialTick, packedLight).argbInt()
			)
		}
	}

	companion object {
		private const val ATTACHMENT_OFFSET = 1.0
	}
}