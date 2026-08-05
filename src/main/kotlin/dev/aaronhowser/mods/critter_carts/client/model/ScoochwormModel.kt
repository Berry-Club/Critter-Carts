package dev.aaronhowser.mods.critter_carts.client.model

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import software.bernie.geckolib.model.DefaultedEntityGeoModel

class ScoochwormModel : DefaultedEntityGeoModel<ScoochwormEntity>(
	CritterCarts.modResource("scoochworm_head")
)