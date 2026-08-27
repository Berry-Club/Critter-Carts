package dev.aaronhowser.mods.critter_carts.client.model.entity

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import dev.aaronhowser.mods.critter_carts.entity.data.attachment.LockboxAttachment
import net.minecraft.util.Mth
import software.bernie.geckolib.model.DefaultedEntityGeoModel

class ScoochwormLockboxModel : DefaultedEntityGeoModel<ScoochwormPartEntity>(
	CritterCarts.modResource("scoochworm/lockbox")
) {

	fun updateLid(
		lockbox: LockboxAttachment?,
		partialTick: Float
	) {
		val progress = Mth.lerp(
			partialTick,
			lockbox?.previousOpenProgress ?: 0f,
			lockbox?.openProgress ?: 0f
		)

		val easedProgress = 1f - (1f - progress) * (1f - progress) * (1f - progress)

		val top = animationProcessor.getBone("top")
		top.rotX = Mth.HALF_PI * easedProgress
	}
}