package dev.aaronhowser.mods.critter_carts.client.model

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import software.bernie.geckolib.model.DefaultedEntityGeoModel

class ScoochwormWickerBasketModel : DefaultedEntityGeoModel<ScoochwormPartEntity>(
	CritterCarts.modResource("scoochworm/wicker_basket")
)