package dev.aaronhowser.mods.critter_carts.entity

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps

data class ScoochwormSegment(
	var attachment: ScoochwormPartAttachment = ScoochwormPartAttachment.NONE
) {

	fun save(): CompoundTag {
		val tag = CompoundTag()
		val encodedAttachment = ScoochwormPartAttachment.CODEC
			.encodeStart(NbtOps.INSTANCE, attachment)
			.result()

		if (encodedAttachment.isPresent) {
			tag.put(ATTACHMENT_TAG, encodedAttachment.get())
		}

		return tag
	}

	companion object {
		private const val ATTACHMENT_TAG = "Attachment"

		fun load(tag: CompoundTag): ScoochwormSegment {
			val attachmentTag = tag.get(ATTACHMENT_TAG)
				?: return ScoochwormSegment()

			val attachment = ScoochwormPartAttachment.CODEC
				.parse(NbtOps.INSTANCE, attachmentTag)
				.result()
				.orElse(ScoochwormPartAttachment.NONE)

			return ScoochwormSegment(attachment)
		}
	}
}