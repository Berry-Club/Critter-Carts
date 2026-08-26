package dev.aaronhowser.mods.critter_carts.entity.data

import com.mojang.serialization.Codec
import dev.aaronhowser.mods.critter_carts.CritterCarts
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.util.StringRepresentable

enum class WormColor(
	val colorName: String,
	val tintColor: Int
) : StringRepresentable {
	GREEN("green", 0xFF55FF55.toInt()),
	BLUE("blue", 0xFF5555FF.toInt()),
	RED("red", 0xFFFF5555.toInt()),
	YELLOW("yellow", 0xFFFFFF55.toInt()),
	MAGENTA("magenta", 0xFFFF55FF.toInt()),
	CYAN("cyan", 0xFF55FFFF.toInt());

	val headTexture = CritterCarts.modResource("textures/entity/scoochworm/$colorName/head.png")
	val bodyTexture = CritterCarts.modResource("textures/entity/scoochworm/$colorName/body.png")

	override fun getSerializedName(): String = colorName

	fun next(): WormColor {
		return entries[(ordinal + 1) % entries.size]
	}

	companion object {
		val CODEC: Codec<WormColor> = StringRepresentable.fromEnum(::values)

		val STREAM_CODEC: StreamCodec<ByteBuf, WormColor> =
			ByteBufCodecs.idMapper(
				WormColor::fromOrdinal,
				WormColor::ordinal
			)

		fun fromOrdinal(ordinal: Int): WormColor {
			return entries.getOrElse(ordinal) { GREEN }
		}
	}
}