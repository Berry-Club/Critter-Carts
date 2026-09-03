package dev.aaronhowser.mods.critter_carts.client.render.block_entity

import com.mojang.blaze3d.vertex.PoseStack
import dev.aaronhowser.mods.aaron.misc.AaronDsls.withPose
import dev.aaronhowser.mods.critter_carts.block_entity.HoppingSpider
import dev.aaronhowser.mods.critter_carts.block_entity.HoppingSpiderNestBlockEntity
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

class HoppingSpiderNestBlockRenderer(
	context: BlockEntityRendererProvider.Context
) : BlockEntityRenderer<HoppingSpiderNestBlockEntity> {

	override fun render(
		blockEntity: HoppingSpiderNestBlockEntity,
		partialTick: Float,
		poseStack: PoseStack,
		bufferSource: MultiBufferSource,
		packedLight: Int,
		packedOverlay: Int
	) {
		for (index in blockEntity.hoppingSpiders.indices) {
			val level = blockEntity.level ?: continue
			val spider = blockEntity.hoppingSpiders[index]
			val position = spider.position ?: blockEntity.blockPos.center
			val displayPosition = position.add(getDisplayOffset(index))
			val localPosition = displayPosition.subtract(Vec3.atLowerCornerOf(blockEntity.blockPos))
			val spiderLight = LevelRenderer.getLightColor(level, BlockPos.containing(displayPosition))

			poseStack.withPose {
				poseStack.translate(localPosition.x, localPosition.y, localPosition.z)
				renderSpider(spider, poseStack, bufferSource, spiderLight, packedOverlay)
			}
		}
	}

	private fun renderSpider(
		spider: HoppingSpider,
		poseStack: PoseStack,
		bufferSource: MultiBufferSource,
		packedLight: Int,
		packedOverlay: Int
	) {
		poseStack.withPose {
			poseStack.translate(-SPIDER_SCALE / 2.0, -SPIDER_SCALE / 2.0, -SPIDER_SCALE / 2.0)
			poseStack.scale(SPIDER_SCALE, SPIDER_SCALE, SPIDER_SCALE)
			Minecraft.getInstance().blockRenderer.renderSingleBlock(
				Blocks.GRAY_CONCRETE.defaultBlockState(),
				poseStack,
				bufferSource,
				packedLight,
				packedOverlay
			)
		}

		if (spider.carriedStack.isEmpty) return

		poseStack.withPose {
			poseStack.translate(0.0, ITEM_HEIGHT, 0.0)
			poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE)
			Minecraft.getInstance().itemRenderer.renderStatic(
				spider.carriedStack,
				ItemDisplayContext.GROUND,
				packedLight,
				packedOverlay,
				poseStack,
				bufferSource,
				blockEntityLevel,
				0
			)
		}
	}

	private fun getDisplayOffset(index: Int): Vec3 {
		val offsetX = if (index % 2 == 0) -IDLE_OFFSET else IDLE_OFFSET
		val offsetZ = if (index / 2 == 0) -IDLE_OFFSET else IDLE_OFFSET
		return Vec3(offsetX, 0.0, offsetZ)
	}

	override fun getRenderBoundingBox(blockEntity: HoppingSpiderNestBlockEntity): AABB {
		return AABB.INFINITE
	}

	private val blockEntityLevel
		get() = Minecraft.getInstance().level

	companion object {
		private const val SPIDER_SCALE = 0.12f
		private const val ITEM_SCALE = 0.4f
		private const val ITEM_HEIGHT = 0.18
		private const val IDLE_OFFSET = 0.14
	}
}