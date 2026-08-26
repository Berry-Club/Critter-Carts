package dev.aaronhowser.mods.critter_carts.client.render.entity.layer

import dev.aaronhowser.mods.critter_carts.client.model.entity.ScoochwormSaddlebagModel
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import dev.aaronhowser.mods.critter_carts.entity.data.attachment.ScoochwormAttachmentType
import software.bernie.geckolib.renderer.GeoRenderer

class ScoochwormSaddlebagLayer(
	renderer: GeoRenderer<ScoochwormPartEntity>
) : ScoochwormPartAttachmentLayer(
	renderer,
	ScoochwormAttachmentType.SADDLEBAGS,
	ScoochwormSaddlebagModel()
)
