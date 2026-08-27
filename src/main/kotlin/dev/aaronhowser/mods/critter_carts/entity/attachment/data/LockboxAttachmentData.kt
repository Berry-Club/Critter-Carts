package dev.aaronhowser.mods.critter_carts.entity.attachment.data

import dev.aaronhowser.mods.critter_carts.entity.attachment.ScoochwormAttachmentType
import dev.aaronhowser.mods.critter_carts.registry.ModScoochwormAttachmentTypes
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

data class LockboxAttachmentData(
	val isOpen: Boolean
) : SyncedAttachmentData {
	override val type: ScoochwormAttachmentType<LockboxAttachmentData>
		get() = ModScoochwormAttachmentTypes.LOCKBOX.get()

	companion object {
		val STREAM_CODEC: StreamCodec<ByteBuf, LockboxAttachmentData> = ByteBufCodecs.BOOL.map(
			::LockboxAttachmentData,
			LockboxAttachmentData::isOpen
		)
	}
}