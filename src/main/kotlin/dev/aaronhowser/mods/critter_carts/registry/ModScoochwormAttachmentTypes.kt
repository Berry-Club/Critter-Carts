package dev.aaronhowser.mods.critter_carts.registry

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isItem
import dev.aaronhowser.mods.critter_carts.CritterCarts
import dev.aaronhowser.mods.critter_carts.datagen.tag.ModItemTagsProvider
import dev.aaronhowser.mods.critter_carts.entity.attachment.ScoochwormAttachment
import dev.aaronhowser.mods.critter_carts.entity.attachment.ScoochwormAttachmentType
import dev.aaronhowser.mods.critter_carts.entity.attachment.builtin.LockboxAttachment
import dev.aaronhowser.mods.critter_carts.entity.attachment.builtin.NoAttachment
import dev.aaronhowser.mods.critter_carts.entity.attachment.builtin.SaddleAttachment
import dev.aaronhowser.mods.critter_carts.entity.attachment.data.LockboxAttachmentData
import dev.aaronhowser.mods.critter_carts.entity.attachment.data.NoAttachmentData
import dev.aaronhowser.mods.critter_carts.entity.attachment.data.SaddleAttachmentData
import dev.aaronhowser.mods.critter_carts.entity.attachment.data.SynchedAttachmentData
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object ModScoochwormAttachmentTypes {

	val REGISTRY_KEY: ResourceKey<Registry<ScoochwormAttachmentType<*>>> = ResourceKey.createRegistryKey(
		CritterCarts.modResource("scoochworm_attachment_type")
	)

	val SCOOCHWORM_ATTACHMENT_TYPE_REGISTRY: DeferredRegister<ScoochwormAttachmentType<*>> =
		DeferredRegister.create(REGISTRY_KEY, CritterCarts.MOD_ID)

	val REGISTRY: Registry<ScoochwormAttachmentType<*>> = SCOOCHWORM_ATTACHMENT_TYPE_REGISTRY.makeRegistry {
		it.sync(true)
	}

	val NONE: DeferredHolder<ScoochwormAttachmentType<*>, ScoochwormAttachmentType<NoAttachmentData>> =
		register("none") {
			ScoochwormAttachmentType(
				streamCodec = NoAttachmentData.STREAM_CODEC,
				itemPredicate = { false },
				attachmentFactory = { NoAttachment() },
				clientAttachmentFactory = ::NoAttachment,
				interactionPredictor = { _, _, heldStack ->
					if (ScoochwormAttachment.canAttach(heldStack)) {
						InteractionResult.SUCCESS
					} else {
						InteractionResult.PASS
					}
				}
			)
		}

	val LOCKBOX: DeferredHolder<ScoochwormAttachmentType<*>, ScoochwormAttachmentType<LockboxAttachmentData>> =
		register("lockbox") {
			ScoochwormAttachmentType(
				streamCodec = LockboxAttachmentData.STREAM_CODEC,
				itemPredicate = { itemStack -> itemStack.isItem(ModItems.LOCKBOX) },
				attachmentFactory = ::LockboxAttachment,
				clientAttachmentFactory = { LockboxAttachment(ItemStack.EMPTY) },
				interactionPredictor = { _, _, _ -> InteractionResult.SUCCESS }
			)
		}

	val SADDLE: DeferredHolder<ScoochwormAttachmentType<*>, ScoochwormAttachmentType<SaddleAttachmentData>> =
		register("saddle") {
			ScoochwormAttachmentType(
				streamCodec = SaddleAttachmentData.STREAM_CODEC,
				itemPredicate = { itemStack ->
					itemStack.isItem(ModItemTagsProvider.SCOOCHWORM_SADDLES)
				},
				attachmentFactory = ::SaddleAttachment,
				clientAttachmentFactory = { SaddleAttachment(ItemStack.EMPTY) },
				interactionPredictor = { _, player, _ ->
					if (player.isShiftKeyDown) InteractionResult.PASS else InteractionResult.SUCCESS
				}
			)
		}

	private fun <T : SynchedAttachmentData> register(
		name: String,
		supplier: Supplier<ScoochwormAttachmentType<T>>
	): DeferredHolder<ScoochwormAttachmentType<*>, ScoochwormAttachmentType<T>> {
		return SCOOCHWORM_ATTACHMENT_TYPE_REGISTRY.register(name, supplier)
	}
}