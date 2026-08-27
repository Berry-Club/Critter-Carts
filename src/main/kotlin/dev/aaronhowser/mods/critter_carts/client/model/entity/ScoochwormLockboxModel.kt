package dev.aaronhowser.mods.critter_carts.client.model.entity

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import software.bernie.geckolib.model.DefaultedEntityGeoModel

class ScoochwormLockboxModel : DefaultedEntityGeoModel<ScoochwormPartEntity>(
	CritterCarts.modResource("scoochworm/lockbox")
)