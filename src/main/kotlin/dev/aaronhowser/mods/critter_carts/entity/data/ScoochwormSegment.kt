package dev.aaronhowser.mods.critter_carts.entity.data

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isItem
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.world.SimpleContainer
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemContainerContents
import java.util.Optional

data class ScoochwormSegment(
	var attachmentItem: ItemStack = ItemStack.EMPTY,
	var dyeColor: DyeColor? = null
) {

	val attachment: ScoochwormPartAttachment
		get() = when {
			attachmentItem.isItem(Items.CHEST) -> ScoochwormPartAttachment.CHEST
			attachmentItem.isItem(Items.SADDLE) -> ScoochwormPartAttachment.SADDLE
			else -> ScoochwormPartAttachment.NONE
		}

	val container = SimpleContainer(CONTAINER_SIZE)

	init {
		loadContainer()
		container.addListener {
			updateContainerComponent()
		}
	}

	fun equipAttachmentItem(stack: ItemStack) {
		attachmentItem = stack
		loadContainer()
	}

	fun removeAttachmentItem(): ItemStack {
		updateContainerComponent()

		val removedStack = attachmentItem
		attachmentItem = ItemStack.EMPTY
		container.clearContent()
		return removedStack
	}

	private fun loadContainer() {
		val contents = attachmentItem.getOrDefault(
			DataComponents.CONTAINER,
			ItemContainerContents.EMPTY
		)
		contents.copyInto(container.items)
	}

	private fun updateContainerComponent() {
		if (!attachmentItem.isItem(Items.CHEST)) return

		val contents = ItemContainerContents.fromItems(container.items)
		attachmentItem.set(DataComponents.CONTAINER, contents)
	}

	fun save(): CompoundTag {
		val maybeTag = CODEC
			.encodeStart(NbtOps.INSTANCE, this)
			.result()

		return maybeTag
			.map { it as CompoundTag }
			.orElseGet(::CompoundTag)
	}

	companion object {
		private const val ATTACHMENT_TAG = "Attachment"
		private const val ATTACHMENT_ITEM_TAG = "AttachmentItem"
		private const val DYE_COLOR_TAG = "DyeColor"
		private const val CONTAINER_SIZE = 27

		val CODEC: Codec<ScoochwormSegment> = RecordCodecBuilder.create { instance ->
			instance.group(
				ScoochwormPartAttachment.CODEC
					.optionalFieldOf(ATTACHMENT_TAG, ScoochwormPartAttachment.NONE)
					.forGetter(ScoochwormSegment::attachment),
				ItemStack.OPTIONAL_CODEC
					.optionalFieldOf(ATTACHMENT_ITEM_TAG, ItemStack.EMPTY)
					.forGetter(ScoochwormSegment::attachmentItem),
				DyeColor.CODEC
					.optionalFieldOf(DYE_COLOR_TAG)
					.forGetter { Optional.ofNullable(it.dyeColor) }
			).apply(instance) { attachment, attachmentItem, dyeColor ->
				ScoochwormSegment(
					getAttachmentItem(attachment, attachmentItem),
					dyeColor.orElse(null)
				)
			}
		}

		private fun getAttachmentItem(
			attachment: ScoochwormPartAttachment,
			attachmentItem: ItemStack
		): ItemStack {
			if (!attachmentItem.isEmpty) return attachmentItem

			return when (attachment) {
				ScoochwormPartAttachment.NONE -> ItemStack.EMPTY
				ScoochwormPartAttachment.CHEST -> Items.CHEST.defaultInstance
				ScoochwormPartAttachment.SADDLE -> Items.SADDLE.defaultInstance
			}
		}

		fun load(tag: CompoundTag): ScoochwormSegment {
			return CODEC
				.parse(NbtOps.INSTANCE, tag)
				.result()
				.orElseGet(::ScoochwormSegment)
		}
	}
}