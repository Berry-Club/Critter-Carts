package dev.aaronhowser.mods.critter_carts.client.render.entity.layer

import dev.aaronhowser.mods.critter_carts.client.model.entity.ScoochwormLockboxModel
import dev.aaronhowser.mods.critter_carts.client.render.entity.ScoochwormPartRenderer
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import dev.aaronhowser.mods.critter_carts.entity.attachment.builtin.LockboxAttachment
import dev.aaronhowser.mods.critter_carts.registry.ModScoochwormAttachmentTypes

class ScoochwormLockboxLayer private constructor(
	renderer: ScoochwormPartRenderer,
	private val lockboxModel: ScoochwormLockboxModel
) : ScoochwormPartAttachmentLayer(
	renderer,
	ModScoochwormAttachmentTypes.LOCKBOX.get(),
	lockboxModel
) {

	constructor(renderer: ScoochwormPartRenderer) : this(
		renderer,
		ScoochwormLockboxModel()
	)

	override fun prepareModel(
		animatable: ScoochwormPartEntity,
		partialTick: Float
	) {
		val lockbox = animatable.getAttachment() as? LockboxAttachment
		lockboxModel.updateLid(lockbox, partialTick)
	}
}