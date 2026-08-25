package dev.aaronhowser.mods.critter_carts.event

import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.client.renderer.CritterCageItemRenderer
import dev.aaronhowser.mods.critter_carts.client.renderer.ScoochwormPartRenderer
import dev.aaronhowser.mods.critter_carts.client.renderer.ScoochwormRenderer
import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes
import dev.aaronhowser.mods.critter_carts.registry.ModItems
import net.minecraft.client.resources.model.ModelResourceLocation
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.client.event.ModelEvent
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent

@EventBusSubscriber(
	modid = CritterCarts.MOD_ID,
	value = [Dist.CLIENT]
)
object ClientEvents {

	@SubscribeEvent
	fun registerEntityRenderers(event: EntityRenderersEvent.RegisterRenderers) {
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

	@SubscribeEvent
	fun registerAdditionalModels(event: ModelEvent.RegisterAdditional) {
		event.register(
			ModelResourceLocation.standalone(CritterCageItemRenderer.BASE_MODEL_LOCATION)
		)
	}
}