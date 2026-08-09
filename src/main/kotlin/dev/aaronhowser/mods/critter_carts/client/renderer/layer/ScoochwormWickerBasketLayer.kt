package dev.aaronhowser.mods.critter_carts.client.renderer.layer

import dev.aaronhowser.mods.critter_carts.client.model.ScoochwormWickerBasketModel
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import dev.aaronhowser.mods.critter_carts.entity.data.attachment.ScoochwormAttachmentType
import software.bernie.geckolib.renderer.GeoRenderer

class ScoochwormWickerBasketLayer(
	renderer: GeoRenderer<ScoochwormPartEntity>
) : ScoochwormPartAttachmentLayer(
	renderer,
	ScoochwormAttachmentType.WICKER_BASKET,
	ScoochwormWickerBasketModel()
)