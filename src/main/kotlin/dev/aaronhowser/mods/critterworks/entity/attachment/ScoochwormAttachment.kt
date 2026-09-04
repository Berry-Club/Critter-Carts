package dev.aaronhowser.mods.critterworks.entity.attachment

import com.mojang.serialization.Codec
import dev.aaronhowser.mods.critterworks.entity.ScoochwormPartEntity
import dev.aaronhowser.mods.critterworks.entity.attachment.builtin.NoAttachment
import dev.aaronhowser.mods.critterworks.entity.attachment.data.SyncedAttachmentData
import dev.aaronhowser.mods.critterworks.registry.ModScoochwormAttachmentTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.IItemHandler

abstract class ScoochwormAttachment(
	itemStack: ItemStack
) {

	protected val itemStack: ItemStack = itemStack.copy()

	abstract val syncedData: SyncedAttachmentData
	abstract val equipSound: SoundEvent?
	open val itemHandler: IItemHandler? = null

	open fun interact(
		player: Player,
		heldStack: ItemStack,
		bodyPart: ScoochwormPartEntity
	): InteractionResult = InteractionResult.PASS

	open fun predictInteraction(
		player: Player,
		heldStack: ItemStack,
		bodyPart: ScoochwormPartEntity
	): InteractionResult = InteractionResult.SUCCESS

	open fun clientTick(bodyPart: ScoochwormPartEntity) {}

	open fun serverTick(bodyPart: ScoochwormPartEntity) {}

	open fun applySyncedData(data: SyncedAttachmentData) {}

	protected open fun synchronizeItemStack() {}

	fun remove(): ItemStack {
		synchronizeItemStack()
		return itemStack
	}

	fun save(): CompoundTag {
		synchronizeItemStack()

		val encodedTag = CODEC
			.encodeStart(NbtOps.INSTANCE, itemStack)
			.result()

		return encodedTag
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
			for (type in ModScoochwormAttachmentTypes.REGISTRY) {
				val attachment = type.create(itemStack)
				if (attachment != null) return attachment
			}

			return NoAttachment()
		}

		fun canAttach(itemStack: ItemStack): Boolean {
			for (type in ModScoochwormAttachmentTypes.REGISTRY) {
				if (type.matches(itemStack)) return true
			}

			return false
		}

		fun createClient(data: SyncedAttachmentData): ScoochwormAttachment {
			return data.resolveType().createClientAttachment(data)
		}

		fun load(tag: CompoundTag): ScoochwormAttachment {
			val itemStack = CODEC
				.parse(NbtOps.INSTANCE, tag)
				.result()
				.orElse(ItemStack.EMPTY)

			return fromItemStack(itemStack)
		}
	}
}