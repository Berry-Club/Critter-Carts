package dev.aaronhowser.mods.critter_carts.entity.data

import com.mojang.serialization.Codec
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isItem
import dev.aaronhowser.mods.critter_carts.datagen.tag.ModItemTagsProvider
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemContainerContents
import net.minecraft.world.phys.Vec3

class ScoochwormSegment(
	attachmentItem: ItemStack = ItemStack.EMPTY
) {

	var attachmentItem = attachmentItem
		private set

	var bodyPart: ScoochwormPartEntity? = null
		private set

	val attachment: ScoochwormPartAttachment
		get() = when {
			attachmentItem.isItem(ModItemTagsProvider.SCOOCHWORM_CHEST_ATTACHMENTS) -> ScoochwormPartAttachment.CHEST
			attachmentItem.isItem(ModItemTagsProvider.SCOOCHWORM_SADDLE_ATTACHMENTS) -> ScoochwormPartAttachment.SADDLE
			else -> ScoochwormPartAttachment.NONE
		}

	val container = SimpleContainer(CONTAINER_SIZE)

	init {
		loadContainer()
		container.addListener {
			updateContainerComponent()
		}
	}

	fun updateBodyPart(
		scoochworm: ScoochwormEntity,
		partIndex: Int,
		position: Vec3
	) {
		var bodyPart = this.bodyPart

		if (bodyPart == null || bodyPart.isRemoved) {
			bodyPart = createBodyPart(scoochworm, partIndex, position)
			this.bodyPart = bodyPart
		}

		bodyPart.moveAlongPath(position, scoochworm.xRot)
	}

	fun discardBodyPart() {
		bodyPart?.discard()
		bodyPart = null
	}

	fun setAttachment(stack: ItemStack) {
		attachmentItem = stack
		loadContainer()
		bodyPart?.attachment = attachment
	}

	fun removeAttachment(): ItemStack {
		updateContainerComponent()

		val removedStack = attachmentItem
		attachmentItem = ItemStack.EMPTY
		container.clearContent()
		bodyPart?.attachment = attachment
		return removedStack
	}

	fun dropAttachmentItem(entity: Entity) {
		val attachmentItem = removeAttachment()
		if (attachmentItem.isEmpty) return

		val dropSource = bodyPart ?: entity
		dropSource.spawnAtLocation(attachmentItem)
	}

	private fun createBodyPart(
		scoochworm: ScoochwormEntity,
		partIndex: Int,
		position: Vec3
	): ScoochwormPartEntity {
		val bodyPart = ScoochwormPartEntity(
			ModEntityTypes.SCOOCHWORM_PART.get(),
			scoochworm.level()
		)

		bodyPart.attachTo(
			scoochworm,
			partIndex,
			attachment
		)

		bodyPart.moveTo(
			position.x,
			position.y,
			position.z,
			scoochworm.yRot,
			scoochworm.xRot
		)

		scoochworm.level().addFreshEntity(bodyPart)
		return bodyPart
	}

	private fun loadContainer() {
		val contents = attachmentItem.getOrDefault(
			DataComponents.CONTAINER,
			ItemContainerContents.EMPTY
		)
		contents.copyInto(container.items)
	}

	private fun updateContainerComponent() {
		if (!attachmentItem.isItem(ModItemTagsProvider.SCOOCHWORM_CHEST_ATTACHMENTS)) return

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
		private const val ATTACHMENT_ITEM_TAG = "AttachmentItem"
		private const val CONTAINER_SIZE = 27

		val CODEC: Codec<ScoochwormSegment> = ItemStack.OPTIONAL_CODEC
			.optionalFieldOf(ATTACHMENT_ITEM_TAG, ItemStack.EMPTY)
			.xmap(::ScoochwormSegment, ScoochwormSegment::attachmentItem)
			.codec()

		fun load(tag: CompoundTag): ScoochwormSegment {
			return CODEC
				.parse(NbtOps.INSTANCE, tag)
				.result()
				.orElseGet(::ScoochwormSegment)
		}
	}
}