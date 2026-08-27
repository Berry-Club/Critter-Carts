package dev.aaronhowser.mods.critter_carts.entity.attachment.data

import dev.aaronhowser.mods.critter_carts.entity.attachment.ScoochwormAttachmentType
import dev.aaronhowser.mods.critter_carts.registry.ModScoochwormAttachmentTypes
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation

interface SyncedAttachmentData {

	val type: ScoochwormAttachmentType<out SyncedAttachmentData>

	companion object {
		val STREAM_CODEC: StreamCodec<ByteBuf, SyncedAttachmentData> = object : StreamCodec<ByteBuf, SyncedAttachmentData> {
			override fun decode(buffer: ByteBuf): SyncedAttachmentData {
				val typeId = ResourceLocation.STREAM_CODEC.decode(buffer)
				val type = ModScoochwormAttachmentTypes.REGISTRY.get(typeId)
					?: error("Unknown attachment data type: $typeId")

				return type.decode(buffer)
			}

			override fun encode(buffer: ByteBuf, data: SyncedAttachmentData) {
				val typeId = ModScoochwormAttachmentTypes.REGISTRY.getKey(data.type)
					?: error("Unregistered attachment data type: ${data.type}")

				ResourceLocation.STREAM_CODEC.encode(buffer, typeId)
				data.type.encode(buffer, data)
			}
		}
	}
}