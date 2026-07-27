package dev.aaronhowser.mods.critter_carts.client.renderer

import dev.aaronhowser.mods.critter_carts.client.model.ScoochwormPartModel
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation
import software.bernie.geckolib.renderer.GeoEntityRenderer

class ScoochwormPartRenderer(
	context: EntityRendererProvider.Context
) : GeoEntityRenderer<ScoochwormPartEntity>(context, ScoochwormPartModel()) {

	init {
		withScale(ScoochwormEntity.SIZE)
	}

	override fun getTextureLocation(animatable: ScoochwormPartEntity): ResourceLocation =
		ScoochwormRenderer.TEXTURE
}