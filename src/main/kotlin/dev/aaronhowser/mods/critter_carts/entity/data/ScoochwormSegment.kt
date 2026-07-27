package dev.aaronhowser.mods.critter_carts.entity.data

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.world.item.DyeColor
import java.util.Optional

data class ScoochwormSegment(
	var attachment: ScoochwormPartAttachment = ScoochwormPartAttachment.NONE,
	var dyeColor: DyeColor? = null
) {

	fun save(): CompoundTag {
		val maybeTag = CODEC
			.encodeStart(NbtOps.INSTANCE, this)
			.result()

		return maybeTag
			.map { it as CompoundTag }
			.orElseGet(::CompoundTag)
	}

	companion object {
		private const val ATTACHMENT_TAG = "Attachment"
		private const val DYE_COLOR_TAG = "DyeColor"

		val CODEC: Codec<ScoochwormSegment> = RecordCodecBuilder.create { instance ->
			instance.group(
				ScoochwormPartAttachment.CODEC
					.optionalFieldOf(ATTACHMENT_TAG, ScoochwormPartAttachment.NONE)
					.forGetter(ScoochwormSegment::attachment),
				DyeColor.CODEC
					.optionalFieldOf(DYE_COLOR_TAG)
					.forGetter { Optional.ofNullable(it.dyeColor) }
			).apply(instance) { attachment, dyeColor ->
				ScoochwormSegment(
					attachment,
					dyeColor.orElse(null)
				)
			}
		}

		fun load(tag: CompoundTag): ScoochwormSegment {
			return CODEC
				.parse(NbtOps.INSTANCE, tag)
				.result()
				.orElseGet(::ScoochwormSegment)
		}
	}
}