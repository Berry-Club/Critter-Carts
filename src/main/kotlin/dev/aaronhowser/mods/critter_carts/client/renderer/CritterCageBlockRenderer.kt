package dev.aaronhowser.mods.critter_carts.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.aaronhowser.mods.critter_carts.block.CritterCageBlock
import dev.aaronhowser.mods.critter_carts.block.CritterCageBlockEntity
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import dev.aaronhowser.mods.critter_carts.registry.ModDataComponents
import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes
import dev.aaronhowser.mods.critter_carts.registry.ModItems
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.resources.model.ModelResourceLocation
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack

class CritterCageBlockRenderer(
	context: BlockEntityRendererProvider.Context
) : BlockEntityRenderer<CritterCageBlockEntity> {

	private val itemRenderer = context.itemRenderer

	override fun render(
		blockEntity: CritterCageBlockEntity,
		partialTick: Float,
		poseStack: PoseStack,
		bufferSource: MultiBufferSource,
		packedLight: Int,
		packedOverlay: Int
	) {
		val down = blockEntity.blockState.getValue(CritterCageBlock.DOWN)
		val forward = blockEntity.blockState.getValue(CritterCageBlock.FORWARD)

		poseStack.pushPose()
		poseStack.translate(0.5, 0.5, 0.5)
		poseStack.mulPose(down.rotation)
		poseStack.mulPose(Axis.YP.rotationDegrees(forward.toYRot()))

		val baseModel = itemRenderer.itemModelShaper.modelManager.getModel(
			ModelResourceLocation.standalone(CritterCageItemRenderer.BASE_MODEL_LOCATION)
		)
		itemRenderer.render(
			ItemStack(ModItems.CRITTER_CAGE.get()),
			ItemDisplayContext.NONE,
			false,
			poseStack,
			bufferSource,
			packedLight,
			packedOverlay,
			baseModel
		)
		poseStack.popPose()

		val data = blockEntity.entityData ?: return
		val level = blockEntity.level ?: return
		val worm = ScoochwormEntity(ModEntityTypes.SCOOCHWORM.get(), level)
		worm.load(data.copyTag())
		worm.supportDirection = down
		val yaw = ScoochwormEntity.getMovementYaw(forward, down)
		worm.yRot = yaw
		worm.yRotO = yaw
		worm.yBodyRot = yaw
		worm.yHeadRot = yaw
		worm.yHeadRotO = yaw

		poseStack.pushPose()
		poseStack.translate(0.5, 0.05, 0.5)
		poseStack.scale(HEAD_SCALE, HEAD_SCALE, HEAD_SCALE)
		Minecraft.getInstance().entityRenderDispatcher.getRenderer(worm).render(
			worm,
			yaw,
			partialTick,
			poseStack,
			bufferSource,
			packedLight
		)
		poseStack.popPose()
	}

	companion object {
		private const val HEAD_SCALE = 0.65f
	}
}