package dev.aaronhowser.mods.critter_carts.client.render.entity.layer

import dev.aaronhowser.mods.critter_carts.client.model.entity.ScoochwormLockboxModel
import dev.aaronhowser.mods.critter_carts.client.render.entity.ScoochwormPartRenderer
import dev.aaronhowser.mods.critter_carts.entity.data.attachment.ScoochwormAttachmentType

class ScoochwormLockboxLayer(
	renderer: ScoochwormPartRenderer
) : ScoochwormPartAttachmentLayer(
	renderer,
	ScoochwormAttachmentType.LOCKBOX,
	ScoochwormLockboxModel()
)