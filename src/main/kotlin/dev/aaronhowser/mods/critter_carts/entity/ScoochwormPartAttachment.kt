package dev.aaronhowser.mods.critter_carts.entity

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.util.StringRepresentable

enum class ScoochwormPartAttachment(
	private val serializedName: String
) : StringRepresentable {
	NONE("none"),
	CHEST("chest"),
	SADDLE("saddle");

	override fun getSerializedName(): String = serializedName

	companion object {
		val CODEC: Codec<ScoochwormPartAttachment> =
			StringRepresentable.fromEnum(::values)

		val STREAM_CODEC: StreamCodec<ByteBuf, ScoochwormPartAttachment> =
			ByteBufCodecs.idMapper(
				{ networkId -> entries[networkId] },
				ScoochwormPartAttachment::ordinal
			)
	}
}