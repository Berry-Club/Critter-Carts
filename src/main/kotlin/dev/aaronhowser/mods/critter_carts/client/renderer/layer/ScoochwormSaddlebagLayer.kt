package dev.aaronhowser.mods.critter_carts.client.renderer.layer

import dev.aaronhowser.mods.critter_carts.client.model.ScoochwormSaddlebagModel
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import dev.aaronhowser.mods.critter_carts.entity.data.ScoochwormPartAttachment
import software.bernie.geckolib.renderer.GeoRenderer

class ScoochwormSaddlebagLayer(
	renderer: GeoRenderer<ScoochwormPartEntity>
) : ScoochwormPartAttachmentLayer(
	renderer,
	ScoochwormPartAttachment.ITEM_STORAGE,
	ScoochwormSaddlebagModel()
)