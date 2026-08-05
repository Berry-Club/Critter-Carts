package dev.aaronhowser.mods.critter_carts.client.renderer

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.client.model.ScoochwormModel
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation
import software.bernie.geckolib.renderer.GeoEntityRenderer

class ScoochwormRenderer(
	context: EntityRendererProvider.Context
) : GeoEntityRenderer<ScoochwormEntity>(context, ScoochwormModel()) {

	init {
		withScale(ScoochwormEntity.SIZE)
	}

	override fun getTextureLocation(animatable: ScoochwormEntity): ResourceLocation = TEXTURE

	companion object {
		val TEXTURE: ResourceLocation = CritterCarts.modResource("textures/entity/scoochworm/head.png")
	}
}