package dev.aaronhowser.mods.critter_carts.entity.attachment.data

import dev.aaronhowser.mods.critter_carts.entity.attachment.ScoochwormAttachmentType
import dev.aaronhowser.mods.critter_carts.registry.ModScoochwormAttachmentTypes
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec

data object NoAttachmentData : SyncedAttachmentData {
	override val type: ScoochwormAttachmentType<NoAttachmentData>
		get() = ModScoochwormAttachmentTypes.NONE.get()

	val STREAM_CODEC: StreamCodec<ByteBuf, NoAttachmentData> = StreamCodec.unit(this)
}