package dev.aaronhowser.mods.critter_carts.entity

import net.minecraft.nbt.CompoundTag

data class ScoochwormSegment(
	var attachment: ScoochwormPartAttachment = ScoochwormPartAttachment.NONE
) {

	fun save(): CompoundTag {
		val tag = CompoundTag()
		tag.putString(ATTACHMENT_TAG, attachment.serializedName)
		return tag
	}

	companion object {
		private const val ATTACHMENT_TAG = "Attachment"

		fun load(tag: CompoundTag): ScoochwormSegment {
			return ScoochwormSegment(
				ScoochwormPartAttachment.fromSerializedName(
					tag.getString(ATTACHMENT_TAG)
				)
			)
		}
	}
}