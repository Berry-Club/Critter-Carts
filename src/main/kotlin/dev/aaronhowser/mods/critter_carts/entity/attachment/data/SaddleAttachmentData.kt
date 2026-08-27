package dev.aaronhowser.mods.critter_carts.entity.attachment.data

import dev.aaronhowser.mods.critter_carts.registry.ModScoochwormAttachmentTypes
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec

data object SaddleAttachmentData : SyncedAttachmentData {
	override val typeId = ModScoochwormAttachmentTypes.SADDLE.id

	val STREAM_CODEC: StreamCodec<ByteBuf, SaddleAttachmentData> = StreamCodec.unit(this)
}