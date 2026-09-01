package dev.aaronhowser.mods.critter_carts.event

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes
import dev.aaronhowser.mods.critter_carts.registry.ModPotions
import dev.aaronhowser.mods.critter_carts.handler.web.WebSavedData
import dev.aaronhowser.mods.critter_carts.packet.ModPacketHandler
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent
import net.neoforged.neoforge.event.level.ChunkWatchEvent
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent

@EventBusSubscriber(modid = CritterCarts.MOD_ID)
object CommonEvents {
	@SubscribeEvent
	fun registerPayloads(event: RegisterPayloadHandlersEvent) {
		ModPacketHandler.registerPayloads(event)
	}

	@SubscribeEvent
	fun onChunkWatch(event: ChunkWatchEvent.Watch) {
		WebSavedData.get(event.player.serverLevel()).syncChunk(event.player, event.pos)
	}

	@SubscribeEvent
	fun onChunkUnwatch(event: ChunkWatchEvent.UnWatch) {
		WebSavedData.get(event.player.serverLevel()).forgetChunk(event.player, event.pos)
	}

	@SubscribeEvent
	fun onServerTick(event: ServerTickEvent.Post) {
		if (event.server.tickCount % WEB_VALIDATION_INTERVAL != 0) return

		val level = event.server.overworld()
		WebSavedData.get(level).removeInvalidLines(level)
	}

	@SubscribeEvent
	fun registerEntityAttributes(event: EntityAttributeCreationEvent) {
		event.put(ModEntityTypes.SCOOCHWORM.get(), ScoochwormEntity.createAttributes())
	}

	@SubscribeEvent
	fun registerCapabilities(event: RegisterCapabilitiesEvent) {
		event.registerEntity(
			Capabilities.ItemHandler.ENTITY,
			ModEntityTypes.SCOOCHWORM_PART.get()
		) { bodyPart, _ -> bodyPart.getItemHandler() }

		event.registerEntity(
			Capabilities.ItemHandler.ENTITY_AUTOMATION,
			ModEntityTypes.SCOOCHWORM_PART.get()
		) { bodyPart, _ -> bodyPart.getItemHandler() }
	}

	@SubscribeEvent
	fun registerBrewingRecipes(event: RegisterBrewingRecipesEvent) {
		ModPotions.registerRecipes(event)
	}

	private const val WEB_VALIDATION_INTERVAL = 20
}