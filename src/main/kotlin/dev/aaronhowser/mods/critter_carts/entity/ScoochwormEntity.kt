package dev.aaronhowser.mods.critter_carts.entity

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.getEquipmentSlot
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isClientSide
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isItem
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isServerSide
import dev.aaronhowser.mods.critter_carts.entity.control.ScoochwormMoveControl
import dev.aaronhowser.mods.critter_carts.entity.goal.ScoochstemFollowGoal
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
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

		if (heldStack.isItem(Items.MELON) && bodySegments.canGrow) {
			if (isServerSide) {
				bodySegments.grow()
				heldStack.consume(1, player)

				playSound(SoundEvents.GENERIC_EAT, 1f, 1f)
				gameEvent(GameEvent.EAT, player)
			}

			return InteractionResult.sidedSuccess(isClientSide)
		}

		if (partIndex != null && heldStack.isItem(Items.SHEARS) && bodySegments.contains(partIndex)) {
			if (isServerSide) {
				bodySegments.removeFrom(partIndex)

				val equipmentSlot = hand.getEquipmentSlot()
				heldStack.hurtAndBreak(1, player, equipmentSlot)

				playSound(SoundEvents.SHEEP_SHEAR, 1f, 1f)
				gameEvent(GameEvent.SHEAR, player)
			}

			return InteractionResult.sidedSuccess(isClientSide)
		}

		return InteractionResult.PASS
	}

	// Collision

	override fun canBeCollidedWith(): Boolean = !isDeadOrDying
	override fun isPushable(): Boolean = false
	override fun isPushedByFluid(type: FluidType): Boolean = false
	override fun getPistonPushReaction(): PushReaction = PushReaction.IGNORE
	override fun knockback(strength: Double, x: Double, z: Double) {}
	override fun push(entity: Entity) {}

	// Entity data

	override fun readAdditionalSaveData(tag: CompoundTag) {
		super.readAdditionalSaveData(tag)

		movementPath.clear()

		val segmentCount = if (tag.contains(SEGMENT_COUNT_TAG, Tag.TAG_INT.toInt())) {
			tag.getInt(SEGMENT_COUNT_TAG)
		} else if (tag.contains(LEGACY_SEGMENT_POSITIONS_TAG, Tag.TAG_LIST.toInt())) {
			tag.getList(LEGACY_SEGMENT_POSITIONS_TAG, Tag.TAG_COMPOUND.toInt())
				.size
		} else {
			1
		}

		bodySegments.restoreCount(segmentCount)
	}

	override fun addAdditionalSaveData(tag: CompoundTag) {
		super.addAdditionalSaveData(tag)
		tag.putInt(SEGMENT_COUNT_TAG, bodySegments.count)
	}

	// Animation

	override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
	}

	override fun getAnimatableInstanceCache(): AnimatableInstanceCache = animatableInstanceCache

	companion object {
		const val SIZE = 14f / 16f
		const val PART_SPACING = SIZE * 1.2

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