package dev.aaronhowser.mods.critter_carts.entity.data

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

enum class ScoochwormPartAttachment {
	NONE,
	ITEM_STORAGE,
	SADDLE;

	companion object {
		val STREAM_CODEC: StreamCodec<ByteBuf, ScoochwormPartAttachment> =
			ByteBufCodecs.idMapper(
				{ networkId -> entries[networkId] },
				ScoochwormPartAttachment::ordinal
			)
	}
}