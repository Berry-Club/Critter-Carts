package dev.aaronhowser.mods.critter_carts.entity.data

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.getEquipmentSlot
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isItem
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.nextRange
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import dev.aaronhowser.mods.critter_carts.entity.data.attachment.*
import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.Vec3

// The segment is the actual thing that gets saved to the head entity
// It holds the attachment etc
class ScoochwormSegment {

	private var attachment: ScoochwormAttachment = NoAttachment()

	// The segment owns the entity, not the other way around
	// The entity doesn't even save anything, actually
	var bodyPart: ScoochwormPartEntity? = null
		private set

	fun updateBodyPart(
		scoochworm: ScoochwormEntity,
		partIndex: Int,
		pathPoint: ScoochwormPathPoint
	) {
		var bodyPart = this.bodyPart

		if (bodyPart == null || bodyPart.isRemoved) {
			bodyPart = createBodyPart(scoochworm, partIndex, pathPoint.position)
			this.bodyPart = bodyPart
		}

		bodyPart.moveAlongPath(
			pathPoint.position,
			pathPoint.supportDirection,
			pathPoint.forwardDirection
		)
	}

	fun discardBodyPart() {
		bodyPart?.discard()
		bodyPart = null
	}

	private fun installAttachment(
		itemStack: ItemStack,
		player: Player,
		bodyPart: ScoochwormPartEntity
	) {
		attachment = ScoochwormAttachment.fromItemStack(itemStack)
		bodyPart.attachmentType = attachment.type

		val equipSound = attachment.equipSound ?: return
		bodyPart.playSound(
			equipSound,
			1f,
			bodyPart.random.nextRange(0.8f, 1.2f)
		)
		bodyPart.gameEvent(GameEvent.EQUIP, player)
	}

	private fun removeAttachment(): ItemStack {
		val removedItem = attachment.remove()
		attachment = NoAttachment()
		bodyPart?.attachmentType = attachment.type
		return removedItem
	}

	fun interact(
		player: Player,
		hand: InteractionHand,
		heldStack: ItemStack,
		onSheared: () -> Unit
	): InteractionResult {
		val bodyPart = bodyPart ?: return InteractionResult.PASS

		if (heldStack.isItem(Items.SHEARS)) {
			bodyPart.playSound(SoundEvents.SHEEP_SHEAR, 1f, 1f)
			bodyPart.gameEvent(GameEvent.SHEAR, player)
			onSheared()

			val equipmentSlot = hand.getEquipmentSlot()
			heldStack.hurtAndBreak(1, player, equipmentSlot)
			return InteractionResult.CONSUME
		}

		if (
			attachment.type != ScoochwormAttachmentType.NONE
			&& player.isShiftKeyDown
			&& heldStack.isEmpty
		) {
			val attachmentItem = removeAttachment()

			if (!player.addItem(attachmentItem)) {
				player.drop(attachmentItem, false)
			}

			bodyPart.playSound(
				SoundEvents.ITEM_FRAME_REMOVE_ITEM,
				1f,
				bodyPart.random.nextRange(0.8f, 1.2f)
			)
			bodyPart.gameEvent(GameEvent.UNEQUIP, player)
			return InteractionResult.CONSUME
		}

		return when (val result = attachment.interact(player, heldStack, bodyPart)) {
			AttachmentInteractionResult.Pass -> InteractionResult.PASS
			AttachmentInteractionResult.Consume -> InteractionResult.CONSUME

			is AttachmentInteractionResult.Install -> {
				// Store a single-item copy in the attachment before consuming the held stack.
				installAttachment(result.itemStack, player, bodyPart)
				heldStack.consume(1, player)
				InteractionResult.CONSUME
			}
		}
	}

	fun dropAttachmentItem(entity: Entity) {
		val attachmentItem = removeAttachment()
		if (attachmentItem.isEmpty) return

		val dropSource = bodyPart ?: entity
		dropSource.spawnAtLocation(attachmentItem)
	}

	fun insertIntoWickerBasket(itemStack: ItemStack): ItemStack {
		val wickerBasket = attachment as? WickerBasketAttachment ?: return itemStack
		return wickerBasket.insert(itemStack)
	}

	fun clientTick() {
		val bodyPart = bodyPart ?: return
		attachment.clientTick(bodyPart)
	}

	fun serverTick() {
		val bodyPart = bodyPart ?: return
		attachment.serverTick(bodyPart)
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
			attachment.type
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

	fun save(): CompoundTag {
		return attachment.save()
	}

	companion object {
		fun predictInteraction(
			player: Player,
			heldStack: ItemStack,
			attachmentType: ScoochwormAttachmentType
		): InteractionResult {
			if (heldStack.isItem(Items.SHEARS)) return InteractionResult.SUCCESS

			if (
				attachmentType != ScoochwormAttachmentType.NONE
				&& player.isShiftKeyDown
				&& heldStack.isEmpty
			) {
				return InteractionResult.SUCCESS
			}

			return ScoochwormAttachment.predictInteraction(
				player,
				heldStack,
				attachmentType
			)
		}

		fun load(tag: CompoundTag): ScoochwormSegment {
			val segment = ScoochwormSegment()
			segment.attachment = ScoochwormAttachment.load(tag)
			return segment
		}
	}
}