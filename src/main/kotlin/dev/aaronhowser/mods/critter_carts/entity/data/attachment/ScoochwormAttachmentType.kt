package dev.aaronhowser.mods.critter_carts.entity.data.attachment

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

enum class ScoochwormAttachmentType {
	NONE,
	LOCKBOX,
	SADDLE;

	companion object {
		val STREAM_CODEC: StreamCodec<ByteBuf, ScoochwormAttachmentType> =
			ByteBufCodecs.idMapper(
				{ networkId -> entries[networkId] },
				ScoochwormAttachmentType::ordinal
			)
	}
}