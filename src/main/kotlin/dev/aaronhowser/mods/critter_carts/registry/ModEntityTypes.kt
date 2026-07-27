package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object ModEntityTypes {

	val ENTITY_TYPE_REGISTRY: DeferredRegister<EntityType<*>> =
		DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, CritterCarts.MOD_ID)

	val SCOOCHWORM: DeferredHolder<EntityType<*>, EntityType<ScoochwormEntity>> =
		ENTITY_TYPE_REGISTRY.register("scoochworm", Supplier {
			EntityType.Builder.of(::ScoochwormEntity, MobCategory.CREATURE)
				.sized(ScoochwormEntity.SIZE, ScoochwormEntity.SIZE)
				.build("scoochworm")
		})

	val SCOOCHWORM_PART: DeferredHolder<EntityType<*>, EntityType<ScoochwormPartEntity>> =
		ENTITY_TYPE_REGISTRY.register("scoochworm_part", Supplier {
			EntityType.Builder.of(::ScoochwormPartEntity, MobCategory.MISC)
				.sized(ScoochwormEntity.SIZE, ScoochwormEntity.SIZE)
				.clientTrackingRange(10)
				.updateInterval(1)
				.build("scoochworm_part")
		})
}