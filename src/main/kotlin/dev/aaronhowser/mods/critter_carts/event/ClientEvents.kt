package dev.aaronhowser.mods.critter_carts.event

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.client.render.bewlr.CritterCageItemRenderer
import dev.aaronhowser.mods.critter_carts.client.render.block_entity.CritterCageBlockRenderer
import dev.aaronhowser.mods.critter_carts.client.render.entity.ScoochwormPartRenderer
import dev.aaronhowser.mods.critter_carts.client.render.entity.ScoochwormRenderer
import dev.aaronhowser.mods.critter_carts.handler.web.ClientWebLines
import dev.aaronhowser.mods.critter_carts.registry.ModBlockEntityTypes
import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes
import dev.aaronhowser.mods.critter_carts.registry.ModItems
import net.minecraft.world.InteractionResult
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent

@EventBusSubscriber(
	modid = CritterCarts.MOD_ID,
	value = [Dist.CLIENT]
)
object ClientEvents {
	@SubscribeEvent
	fun interactWithWebLine(event: PlayerInteractEvent.RightClickBlock) {
		if (event.isCanceled) return
		if (!ClientWebLines.interact(event.entity, event.hand)) return

		event.cancellationResult = InteractionResult.SUCCESS
		event.isCanceled = true
	}

	@SubscribeEvent
	fun interactWithWebLine(event: PlayerInteractEvent.RightClickItem) {
		if (event.isCanceled) return
		if (!ClientWebLines.interact(event.entity, event.hand)) return

		event.cancellationResult = InteractionResult.SUCCESS
		event.isCanceled = true
	}

	@SubscribeEvent
	fun interactWithWebLine(event: PlayerInteractEvent.EntityInteract) {
		if (event.isCanceled) return
		if (!ClientWebLines.interact(event.entity, event.hand)) return

		event.cancellationResult = InteractionResult.SUCCESS
		event.isCanceled = true
	}

	@SubscribeEvent
	fun onLoggingOut(event: ClientPlayerNetworkEvent.LoggingOut) {
		ClientWebLines.clear()
	}

	@SubscribeEvent
	fun registerEntityRenderers(event: EntityRenderersEvent.RegisterRenderers) {
		event.registerBlockEntityRenderer(ModBlockEntityTypes.CRITTER_CAGE.get(), ::CritterCageBlockRenderer)
		event.registerEntityRenderer(ModEntityTypes.SCOOCHWORM.get(), ::ScoochwormRenderer)
		event.registerEntityRenderer(ModEntityTypes.SCOOCHWORM_PART.get(), ::ScoochwormPartRenderer)
	}

	@SubscribeEvent
	fun registerClientExtensions(event: RegisterClientExtensionsEvent) {
		event.registerItem(
			CritterCageItemRenderer.ClientItemExtensions,
			ModItems.CRITTER_CAGE.get()
		)
	}

}