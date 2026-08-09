package dev.aaronhowser.mods.critter_carts.entity.data

import com.mojang.serialization.Codec
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.getEquipmentSlot
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isItem
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.nextRange
import dev.aaronhowser.mods.critter_carts.datagen.tag.ModItemTagsProvider
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormPartEntity
import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes
import dev.aaronhowser.mods.critter_carts.registry.ModItems
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.SimpleContainer
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemContainerContents
import net.minecraft.world.level.gameevent.GameEvent
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
			attachmentItem.isItem(ModItems.SADDLEBAG) -> ScoochwormPartAttachment.ITEM_STORAGE
			attachmentItem.isItem(ModItemTagsProvider.SCOOCHWORM_SADDLES) -> ScoochwormPartAttachment.SADDLE
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
		pathPoint: ScoochwormPathPoint
	) {
		var bodyPart = this.bodyPart

		if (bodyPart == null || bodyPart.isRemoved) {
			bodyPart = createBodyPart(scoochworm, partIndex, pathPoint.position)
			this.bodyPart = bodyPart
		}

		bodyPart.moveAlongPath(pathPoint.position, pathPoint.bottom)
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
			attachment != ScoochwormPartAttachment.NONE
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

		return when (attachment) {
			ScoochwormPartAttachment.NONE -> addAttachment(player, heldStack, bodyPart)
			ScoochwormPartAttachment.SADDLE -> ride(player, bodyPart)
			ScoochwormPartAttachment.ITEM_STORAGE -> openStorage(player)
		}
	}

	private fun addAttachment(
		player: Player,
		heldStack: ItemStack,
		bodyPart: ScoochwormPartEntity
	): InteractionResult {
		val attachment = getAttachment(heldStack) ?: return InteractionResult.PASS
		setAttachment(heldStack.copyWithCount(1))
		heldStack.consume(1, player)

		val sound = when (attachment) {
			ScoochwormPartAttachment.SADDLE -> SoundEvents.HORSE_SADDLE
			ScoochwormPartAttachment.ITEM_STORAGE -> SoundEvents.DONKEY_CHEST
			ScoochwormPartAttachment.NONE -> return InteractionResult.PASS
		}

		bodyPart.playSound(
			sound,
			1f,
			bodyPart.random.nextRange(0.8f, 1.2f)
		)
		bodyPart.gameEvent(GameEvent.EQUIP, player)
		return InteractionResult.CONSUME
	}

	private fun ride(
		player: Player,
		bodyPart: ScoochwormPartEntity
	): InteractionResult {
		if (player.isShiftKeyDown) return InteractionResult.PASS
		if (!player.startRiding(bodyPart)) return InteractionResult.PASS
		return InteractionResult.CONSUME
	}

	private fun openStorage(player: Player): InteractionResult {
		val menuProvider = SimpleMenuProvider(
			{ containerId, playerInventory, _ ->
				ChestMenu.threeRows(containerId, playerInventory, container)
			},
			ModItems.SADDLEBAG.get().description
		)
		player.openMenu(menuProvider)
		return InteractionResult.CONSUME
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
		if (!attachmentItem.isItem(ModItems.SADDLEBAG)) return

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
		fun predictInteraction(
			player: Player,
			heldStack: ItemStack,
			currentAttachment: ScoochwormPartAttachment
		): InteractionResult {
			if (heldStack.isItem(Items.SHEARS)) return InteractionResult.SUCCESS

			if (
				currentAttachment != ScoochwormPartAttachment.NONE
				&& player.isShiftKeyDown
				&& heldStack.isEmpty
			) {
				return InteractionResult.SUCCESS
			}

			return when (currentAttachment) {
				ScoochwormPartAttachment.NONE -> {
					if (getAttachment(heldStack) == null) {
						InteractionResult.PASS
					} else {
						InteractionResult.SUCCESS
					}
				}

				ScoochwormPartAttachment.SADDLE -> {
					if (player.isShiftKeyDown) {
						InteractionResult.PASS
					} else {
						InteractionResult.SUCCESS
					}
				}

				ScoochwormPartAttachment.ITEM_STORAGE -> InteractionResult.SUCCESS
			}
		}

		private fun getAttachment(heldStack: ItemStack): ScoochwormPartAttachment? {
			return when {
				heldStack.isItem(ModItemTagsProvider.SCOOCHWORM_SADDLES) -> {
					ScoochwormPartAttachment.SADDLE
				}

				heldStack.isItem(ModItems.SADDLEBAG) -> {
					ScoochwormPartAttachment.ITEM_STORAGE
				}

				else -> null
			}
		}

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