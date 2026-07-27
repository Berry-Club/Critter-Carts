package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.entity.data.ScoochwormPartAttachment
import net.minecraft.network.syncher.EntityDataSerializer
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

	val SCOOCHWORM_PART_ATTACHMENT:
			DeferredHolder<EntityDataSerializer<*>, EntityDataSerializer<ScoochwormPartAttachment>> =
		ENTITY_DATA_SERIALIZER_REGISTRY.register(
			"scoochworm_part_attachment",
			Supplier {
				EntityDataSerializer.forValueType(
					ScoochwormPartAttachment.STREAM_CODEC
				)
			}
		)
}