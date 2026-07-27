package dev.aaronhowser.mods.critter_carts.entity

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.getEquipmentSlot
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isClientSide
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isItem
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isServerSide
import dev.aaronhowser.mods.critter_carts.entity.control.ScoochwormMoveControl
import dev.aaronhowser.mods.critter_carts.entity.data.ScoochwormPartAttachment
import dev.aaronhowser.mods.critter_carts.entity.data.ScoochwormPath
import dev.aaronhowser.mods.critter_carts.entity.data.ScoochwormSegments
import dev.aaronhowser.mods.critter_carts.entity.goal.ScoochstemFollowGoal
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.level.material.PushReaction
import net.neoforged.neoforge.fluids.FluidType
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache
import software.bernie.geckolib.animation.AnimatableManager

class ScoochwormEntity(
	entityType: EntityType<ScoochwormEntity>,
	level: Level
) : PathfinderMob(entityType, level), GeoEntity {

	private val animatableInstanceCache = SingletonAnimatableInstanceCache(this)
	val scoochwormMoveControl = ScoochwormMoveControl(this)
	private val movementPath = ScoochwormPath(PART_SPACING * ScoochwormSegments.MAX_COUNT)
	private val bodySegments = ScoochwormSegments(this)

	init {
		moveControl = scoochwormMoveControl
	}

	override fun registerGoals() {
		goalSelector.addGoal(0, ScoochstemFollowGoal(this))
	}

	override fun aiStep() {
		super.aiStep()

		if (isClientSide) return

		movementPath.record(position(), yRot)
		bodySegments.update(movementPath)
	}

	override fun remove(reason: RemovalReason) {
		super.remove(reason)
		bodySegments.discard()
	}

	// Interaction

	override fun mobInteract(player: Player, hand: InteractionHand): InteractionResult {
		return interactWithPart(player, hand, null)
	}

	fun interactWithPart(
		player: Player,
		hand: InteractionHand,
		partIndex: Int?            // null = head
	): InteractionResult {
		val heldStack = player.getItemInHand(hand)

		val growResult = tryGrow(player, heldStack)
		if (growResult != null) return growResult

		val shearResult = tryShear(player, hand, heldStack, partIndex)
		if (shearResult != null) return shearResult

		val addAttachmentResult = tryAddAttachment(player, heldStack, partIndex)
		if (addAttachmentResult != null) return addAttachmentResult

		val removeAttachmentResult = tryRemoveAttachment(player, heldStack, partIndex)
		if (removeAttachmentResult != null) return removeAttachmentResult

		val openChestResult = tryOpenChest(player, partIndex)
		if (openChestResult != null) return openChestResult

		return InteractionResult.PASS
	}

	private fun tryGrow(player: Player, heldStack: ItemStack): InteractionResult? {
		if (!heldStack.isItem(Items.MELON) || !bodySegments.canGrow) return null

		if (isServerSide) {
			bodySegments.grow()
			heldStack.consume(1, player)

			playSound(SoundEvents.GENERIC_EAT, 1f, 1f)
			gameEvent(GameEvent.EAT, player)
		}

		return InteractionResult.sidedSuccess(isClientSide)
	}

	private fun tryShear(
		player: Player,
		hand: InteractionHand,
		heldStack: ItemStack,
		partIndex: Int?
	): InteractionResult? {
		if (partIndex == null) return null
		if (!heldStack.isItem(Items.SHEARS) || !bodySegments.contains(partIndex)) return null

		if (isServerSide) {
			bodySegments.removeFrom(partIndex)

			val equipmentSlot = hand.getEquipmentSlot()
			heldStack.hurtAndBreak(1, player, equipmentSlot)

			playSound(SoundEvents.SHEEP_SHEAR, 1f, 1f)
			gameEvent(GameEvent.SHEAR, player)
		}

		return InteractionResult.sidedSuccess(isClientSide)
	}

	private fun tryAddAttachment(
		player: Player,
		heldStack: ItemStack,
		partIndex: Int?
	): InteractionResult? {
		if (partIndex == null) return null
		if (bodySegments.getAttachment(partIndex) != ScoochwormPartAttachment.NONE) return null

		val attachment = when {
			heldStack.isItem(Items.SADDLE) -> ScoochwormPartAttachment.SADDLE
			heldStack.isItem(Items.CHEST) -> ScoochwormPartAttachment.CHEST
			else -> return null
		}

		if (isServerSide) {
			val attachmentItem = heldStack.copyWithCount(1)
			bodySegments.setAttachmentItem(partIndex, attachmentItem)
			heldStack.consume(1, player)

			@Suppress("KotlinConstantConditions")
			val sound = when (attachment) {
				ScoochwormPartAttachment.SADDLE -> SoundEvents.HORSE_SADDLE
				ScoochwormPartAttachment.CHEST -> SoundEvents.DONKEY_CHEST
				ScoochwormPartAttachment.NONE -> return null
			}

			playSound(sound, 1f, 1f)
			gameEvent(GameEvent.EQUIP, player)
		}

		return InteractionResult.sidedSuccess(isClientSide)
	}

	private fun tryRemoveAttachment(
		player: Player,
		heldStack: ItemStack,
		partIndex: Int?
	): InteractionResult? {
		if (partIndex == null) return null
		if (!player.isShiftKeyDown || !heldStack.isEmpty) return null

		val attachment = bodySegments.getAttachment(partIndex)
		if (attachment == null || attachment == ScoochwormPartAttachment.NONE) return null

		if (isServerSide) {
			val attachmentItem = bodySegments.removeAttachmentItem(partIndex)

			if (!player.addItem(attachmentItem)) {
				player.drop(attachmentItem, false)
			}

			playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1f, 1f)
			gameEvent(GameEvent.UNEQUIP, player)
		}

		return InteractionResult.sidedSuccess(isClientSide)
	}

	private fun tryOpenChest(
		player: Player,
		partIndex: Int?
	): InteractionResult? {
		if (partIndex == null) return null
		if (bodySegments.getAttachment(partIndex) != ScoochwormPartAttachment.CHEST) return null

		if (isServerSide) {
			val container = bodySegments.getContainer(partIndex) ?: return null
			val menuProvider = SimpleMenuProvider(
				{ containerId, playerInventory, _ ->
					ChestMenu.threeRows(containerId, playerInventory, container)
				},
				Items.CHEST.description
			)
			player.openMenu(menuProvider)
		}

		return InteractionResult.sidedSuccess(isClientSide)
	}

	// Collision

	override fun canBeCollidedWith(): Boolean = !isDeadOrDying
	override fun canCollideWith(entity: Entity): Boolean {
		return entity !is ScoochwormPartEntity || entity.parentId != id
	}

	override fun isPushable(): Boolean = false
	override fun isPushedByFluid(type: FluidType): Boolean = false
	override fun getPistonPushReaction(): PushReaction = PushReaction.IGNORE
	override fun knockback(strength: Double, x: Double, z: Double) {}
	override fun push(entity: Entity) {}

	// Entity data

	override fun readAdditionalSaveData(tag: CompoundTag) {
		super.readAdditionalSaveData(tag)

		movementPath.clear()

		if (tag.contains(SEGMENTS_TAG, Tag.TAG_LIST.toInt())) {
			bodySegments.load(
				tag.getList(SEGMENTS_TAG, Tag.TAG_COMPOUND.toInt())
			)
		} else if (tag.contains(SEGMENT_COUNT_TAG, Tag.TAG_INT.toInt())) {
			bodySegments.restoreLegacyCount(
				tag.getInt(SEGMENT_COUNT_TAG)
			)
		} else if (tag.contains(LEGACY_SEGMENT_POSITIONS_TAG, Tag.TAG_LIST.toInt())) {
			bodySegments.restoreLegacyCount(
				tag.getList(
					LEGACY_SEGMENT_POSITIONS_TAG,
					Tag.TAG_COMPOUND.toInt()
				).size
			)
		} else {
			bodySegments.restoreLegacyCount(1)
		}
	}

	override fun addAdditionalSaveData(tag: CompoundTag) {
		super.addAdditionalSaveData(tag)
		tag.put(SEGMENTS_TAG, bodySegments.save())
	}

	// Animation

	override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
	}

	override fun getAnimatableInstanceCache(): AnimatableInstanceCache = animatableInstanceCache

	companion object {
		const val SIZE = 14f / 16f
		const val PART_SPACING = SIZE * 1.2

		private const val SEGMENTS_TAG = "Segments"
		private const val SEGMENT_COUNT_TAG = "SegmentCount"
		private const val LEGACY_SEGMENT_POSITIONS_TAG = "SegmentPositions"

		fun createAttributes(): AttributeSupplier {
			return createMobAttributes()
				.add(Attributes.MAX_HEALTH, 10.0)
				.add(Attributes.MOVEMENT_SPEED, 0.2)
				.build()
		}
	}
}