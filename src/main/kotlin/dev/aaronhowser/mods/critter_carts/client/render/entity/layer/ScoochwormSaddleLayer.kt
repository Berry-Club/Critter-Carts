package dev.aaronhowser.mods.critter_carts.client.render.entity.layer

import dev.aaronhowser.mods.critter_carts.client.model.entity.ScoochwormSaddleModel
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import dev.aaronhowser.mods.critter_carts.registry.ModScoochwormAttachmentTypes
import software.bernie.geckolib.renderer.GeoRenderer

class ScoochwormSaddleLayer(
	renderer: GeoRenderer<ScoochwormPartEntity>
) : ScoochwormPartAttachmentLayer(
	renderer,
	ModScoochwormAttachmentTypes.SADDLE.get(),
	ScoochwormSaddleModel()
)