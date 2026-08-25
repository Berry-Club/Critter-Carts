package dev.aaronhowser.mods.critter_carts.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import dev.aaronhowser.mods.critter_carts.registry.ModDataComponents
import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.resources.model.ModelResourceLocation
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.Level
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions

class CritterCageItemRenderer : BlockEntityWithoutLevelRenderer(
	Minecraft.getInstance().blockEntityRenderDispatcher,
	Minecraft.getInstance().entityModels
) {

	private var cachedData: CustomData? = null
	private var cachedLevel: Level? = null
	private var cachedScoochworm: ScoochwormEntity? = null

	override fun renderByItem(
		stack: ItemStack,
		displayContext: ItemDisplayContext,
		poseStack: PoseStack,
		buffer: MultiBufferSource,
		packedLight: Int,
		packedOverlay: Int
	) {
		renderCage(stack, poseStack, buffer, packedLight, packedOverlay)

		val scoochworm = getScoochworm(stack) ?: return
		prepareScoochworm(scoochworm, displayContext)

		poseStack.pushPose()
		poseStack.translate(0.5, 0.05, 0.5)
		poseStack.scale(HEAD_SCALE, HEAD_SCALE, HEAD_SCALE)

		val entityRenderer = Minecraft.getInstance()
			.entityRenderDispatcher
			.getRenderer(scoochworm)

		entityRenderer.render(
			scoochworm,
			scoochworm.yRot,
			0f,
			poseStack,
			buffer,
			packedLight
		)

		poseStack.popPose()
	}

	private fun renderCage(
		stack: ItemStack,
		poseStack: PoseStack,
		buffer: MultiBufferSource,
		packedLight: Int,
		packedOverlay: Int
	) {
		val itemRenderer = Minecraft.getInstance().itemRenderer
		val modelManager = itemRenderer.itemModelShaper.modelManager
		val baseModel = modelManager.getModel(
			ModelResourceLocation.standalone(BASE_MODEL_LOCATION)
		)

		poseStack.pushPose()
		poseStack.translate(0.5, 0.5, 0.5)

		itemRenderer.render(
			stack,
			ItemDisplayContext.NONE,
			false,
			poseStack,
			buffer,
			packedLight,
			packedOverlay,
			baseModel
		)

		poseStack.popPose()
	}

	private fun getScoochworm(stack: ItemStack): ScoochwormEntity? {
		val entityData = stack.get(ModDataComponents.ENTITY_DATA) ?: return null
		val level = Minecraft.getInstance().level ?: return null

		if (entityData == cachedData && level === cachedLevel) {
			return cachedScoochworm
		}

		val scoochworm = ScoochwormEntity(ModEntityTypes.SCOOCHWORM.get(), level)
		scoochworm.load(entityData.copyTag())

		cachedData = entityData
		cachedLevel = level
		cachedScoochworm = scoochworm

		return scoochworm
	}

	private fun prepareScoochworm(
		scoochworm: ScoochwormEntity,
		displayContext: ItemDisplayContext
	) {
		val yaw = when (displayContext) {
			ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
			ItemDisplayContext.THIRD_PERSON_LEFT_HAND -> THIRD_PERSON_YAW
			else -> DEFAULT_YAW
		}

		scoochworm.supportDirection = Direction.DOWN
		scoochworm.yRot = yaw
		scoochworm.yRotO = yaw
		scoochworm.yBodyRot = yaw
		scoochworm.yHeadRot = yaw
		scoochworm.yHeadRotO = yaw
		scoochworm.xRot = 0f
		scoochworm.xRotO = 0f
	}

	object ClientItemExtensions : IClientItemExtensions {
		private val renderer = CritterCageItemRenderer()

		override fun getCustomRenderer(): BlockEntityWithoutLevelRenderer {
			return renderer
		}
	}

	companion object {
		val BASE_MODEL_LOCATION: ResourceLocation = CritterCarts.modResource("item/critter_cage_base")

		private const val HEAD_SCALE = 0.65f
		private const val THIRD_PERSON_YAW = 0f
		private const val DEFAULT_YAW = 180f
	}
}