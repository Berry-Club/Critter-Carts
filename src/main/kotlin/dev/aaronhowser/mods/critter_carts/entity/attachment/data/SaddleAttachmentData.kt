package dev.aaronhowser.mods.critter_carts.entity.attachment.data

import dev.aaronhowser.mods.critter_carts.entity.attachment.ScoochwormAttachmentType
import dev.aaronhowser.mods.critter_carts.registry.ModScoochwormAttachmentTypes
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec

data object SaddleAttachmentData : SyncedAttachmentData {
	override val type: ScoochwormAttachmentType<SaddleAttachmentData>
		get() = ModScoochwormAttachmentTypes.SADDLE.get()

	val STREAM_CODEC: StreamCodec<ByteBuf, SaddleAttachmentData> = StreamCodec.unit(this)
}