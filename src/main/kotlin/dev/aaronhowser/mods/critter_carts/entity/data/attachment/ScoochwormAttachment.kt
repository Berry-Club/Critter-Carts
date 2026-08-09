package dev.aaronhowser.mods.critter_carts.entity.data.attachment

import com.mojang.serialization.Codec
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isItem
import dev.aaronhowser.mods.critter_carts.datagen.tag.ModItemTagsProvider
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import dev.aaronhowser.mods.critter_carts.registry.ModItems
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

sealed class ScoochwormAttachment(
	itemStack: ItemStack
) {

	protected val itemStack: ItemStack = itemStack.copy()

	abstract val type: ScoochwormAttachmentType
	abstract val equipSound: SoundEvent?

	abstract fun interact(
		player: Player,
		heldStack: ItemStack,
		bodyPart: ScoochwormPartEntity
	): AttachmentInteractionResult

	protected open fun synchronizeItemStack() {}

	fun remove(): ItemStack {
		synchronizeItemStack()
		return itemStack
	}

	fun save(): CompoundTag {
		synchronizeItemStack()

		val maybeTag = CODEC
			.encodeStart(NbtOps.INSTANCE, itemStack)
			.result()

		return maybeTag
			.map { it as CompoundTag }
			.orElseGet(::CompoundTag)
	}

	companion object {
		private const val ATTACHMENT_ITEM_TAG = "AttachmentItem"

		private val CODEC: Codec<ItemStack> = ItemStack.OPTIONAL_CODEC
			.optionalFieldOf(ATTACHMENT_ITEM_TAG, ItemStack.EMPTY)
			.codec()

		fun fromItemStack(
			itemStack: ItemStack
		): ScoochwormAttachment {
			return when {
				itemStack.isItem(ModItems.SADDLEBAG) -> ItemStorageAttachment(itemStack)
				itemStack.isItem(ModItemTagsProvider.SCOOCHWORM_SADDLES) -> SaddleAttachment(itemStack)
				else -> NoAttachment()
			}
		}

		fun canAttach(itemStack: ItemStack): Boolean {
			return itemStack.isItem(ModItems.SADDLEBAG) ||
				itemStack.isItem(ModItemTagsProvider.SCOOCHWORM_SADDLES)
		}

		fun load(tag: CompoundTag): ScoochwormAttachment {
			val itemStack = CODEC
				.parse(NbtOps.INSTANCE, tag)
				.result()
				.orElse(ItemStack.EMPTY)

			return fromItemStack(itemStack)
		}

		fun predictInteraction(
			player: Player,
			heldStack: ItemStack,
			currentType: ScoochwormAttachmentType
		): InteractionResult {
			return when (currentType) {
				ScoochwormAttachmentType.NONE -> {
					if (canAttach(heldStack)) InteractionResult.SUCCESS else InteractionResult.PASS
				}

				ScoochwormAttachmentType.SADDLE -> {
					if (player.isShiftKeyDown) InteractionResult.PASS else InteractionResult.SUCCESS
				}

				ScoochwormAttachmentType.ITEM_STORAGE -> InteractionResult.SUCCESS
			}
		}
	}
}