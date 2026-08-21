package dev.aaronhowser.mods.critter_carts.client.model

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import net.minecraft.util.Mth
import software.bernie.geckolib.animation.AnimationState
import software.bernie.geckolib.cache.`object`.GeoBone
import software.bernie.geckolib.constant.DataTickets
import software.bernie.geckolib.model.DefaultedEntityGeoModel

class ScoochwormModel : DefaultedEntityGeoModel<ScoochwormEntity>(
	CritterCarts.modResource("scoochworm/head")
) {

	override fun setCustomAnimations(
		animatable: ScoochwormEntity,
		instanceId: Long,
		animationState: AnimationState<ScoochwormEntity>
	) {
		val face = animationProcessor.getBone("face")
		val entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA) ?: return

		face.posY = Mth.map(entityData.headPitch, -90f, 90f, -3f, 3f)
		face.posX = Mth.map(entityData.netHeadYaw, -90f, 90f, -2f, 2f)
	}

}