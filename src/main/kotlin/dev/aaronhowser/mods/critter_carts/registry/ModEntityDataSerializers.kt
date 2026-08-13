package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.entity.data.attachment.ScoochwormAttachmentType
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.syncher.EntityDataSerializer
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import java.util.function.Supplier

object ModEntityDataSerializers {

	val ENTITY_DATA_SERIALIZER_REGISTRY: DeferredRegister<EntityDataSerializer<*>> =
		DeferredRegister.create(
			NeoForgeRegistries.ENTITY_DATA_SERIALIZERS,
			CritterCarts.MOD_ID
		)

	val SCOOCHWORM_ATTACHMENT_TYPE: DeferredHolder<EntityDataSerializer<*>, EntityDataSerializer<ScoochwormAttachmentType>> =
		register(
			"scoochworm_attachment_type",
			EntityDataSerializer.forValueType(
				ScoochwormAttachmentType.STREAM_CODEC
			)
		)

	val VEC3: DeferredHolder<EntityDataSerializer<*>, EntityDataSerializer<Vec3>> =
		register(
			"vec3",
			EntityDataSerializer.forValueType(
				StreamCodec.of<ByteBuf, Vec3>(
					{ buffer: ByteBuf, value: Vec3 ->
						buffer.writeFloat(value.x.toFloat())
						buffer.writeFloat(value.y.toFloat())
						buffer.writeFloat(value.z.toFloat())
					},
					{ buffer: ByteBuf ->
						Vec3(
							buffer.readFloat().toDouble(),
							buffer.readFloat().toDouble(),
							buffer.readFloat().toDouble()
						)
					}
				)
			)
		)

	private fun <T : EntityDataSerializer<*>> register(
		name: String,
		serializer: T
	): DeferredHolder<EntityDataSerializer<*>, T> {
		return ENTITY_DATA_SERIALIZER_REGISTRY.register(name, Supplier { serializer })
	}

}