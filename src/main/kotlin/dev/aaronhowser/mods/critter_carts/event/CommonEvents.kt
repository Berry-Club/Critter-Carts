package dev.aaronhowser.mods.critter_carts.event

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isBlock
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.advancement.ModAdvancements
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import dev.aaronhowser.mods.critter_carts.registry.ModBlocks
import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes
import dev.aaronhowser.mods.critter_carts.registry.ModPotions
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent
import net.neoforged.neoforge.event.level.BlockEvent

@EventBusSubscriber(modid = CritterCarts.MOD_ID)
object CommonEvents {

	@SubscribeEvent
	fun afterBlockBroken(event: BlockEvent.BreakEvent) {
		if (!event.state.isBlock(ModBlocks.APPLE_SLICE)) return

		ModAdvancements.award(
			event.player,
			ModAdvancements.BREAK_APPLE_SLICE
		)
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
}