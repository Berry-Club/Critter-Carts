package dev.aaronhowser.mods.critter_carts.entity

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isClientSide
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isItem
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isServerSide
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.nextRange
import dev.aaronhowser.mods.critter_carts.block.ScoochwormTravelBlock
import dev.aaronhowser.mods.critter_carts.entity.control.ScoochwormMoveControl
import dev.aaronhowser.mods.critter_carts.entity.data.attachment.ScoochwormAttachmentType
import dev.aaronhowser.mods.critter_carts.entity.data.ScoochwormPath
import dev.aaronhowser.mods.critter_carts.entity.data.ScoochwormSegments
import dev.aaronhowser.mods.critter_carts.entity.goal.ScoochstemFollowGoal
import dev.aaronhowser.mods.critter_carts.registry.ModSoundEvents
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.level.material.PushReaction
import net.minecraft.world.phys.Vec3
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

	private var footstepPartIndex = HEAD_FOOTSTEP_INDEX
	private var nextFootstepTick = 0

	init {
		moveControl = scoochwormMoveControl
	}

	var attachmentBottom: Direction
		get() = entityData.get(DATA_ATTACHMENT_BOTTOM)
		set(value) = entityData.set(DATA_ATTACHMENT_BOTTOM, value)

	var isMoving: Boolean
		get() = entityData.get(DATA_IS_MOVING)
		private set(value) = entityData.set(DATA_IS_MOVING, value)

	override fun registerGoals() {
		goalSelector.addGoal(0, ScoochstemFollowGoal(this))
	}

	override fun aiStep() {
		isNoGravity = isAttachedToValidBlock()

		super.aiStep()

		if (isClientSide || !isMoving) return

		movementPath.record(position(), attachmentBottom, yRot)
		bodySegments.update(movementPath)

		playNextFootstep()
	}

	fun isAttachedToValidBlock(): Boolean {
		val attachmentPosition = BlockPos.containing(
			position().add(Vec3.atLowerCornerOf(attachmentBottom.normal))
		)

		return supportsScoochwormTravel(
			level(),
			attachmentPosition,
			attachmentBottom.opposite
		)
	}

	private fun playNextFootstep() {
		if (tickCount < nextFootstepTick) return

		val footstepEntity = when (footstepPartIndex) {
			HEAD_FOOTSTEP_INDEX -> this
			else -> bodySegments.getBodyPart(footstepPartIndex)
		} ?: return

		footstepEntity.playSound(
			ModSoundEvents.SCOOCHWORM_FOOTSTEP.get(),
			0.35f,
			random.nextRange(0.85f, 1.15f)
		)

		footstepPartIndex++
		if (footstepPartIndex < bodySegments.size) {
			nextFootstepTick = tickCount + FOOTSTEP_INTERVAL_TICKS
			return
		}

		footstepPartIndex = HEAD_FOOTSTEP_INDEX
		nextFootstepTick = tickCount + FOOTSTEP_CYCLE_PAUSE_TICKS
	}

	override fun remove(reason: RemovalReason) {
		super.remove(reason)
		bodySegments.discard()
	}

	override fun dropEquipment() {
		super.dropEquipment()

		bodySegments.dropAllAttachmentItems()
	}

	// Interaction

	override fun mobInteract(player: Player, hand: InteractionHand): InteractionResult {
		val heldStack = player.getItemInHand(hand)
		val growResult = tryGrow(player, heldStack)
		if (growResult != null) return growResult

		if (isServerSide) {
			isMoving = !isMoving
			if (!isMoving) deltaMovement = Vec3.ZERO
		}

		return InteractionResult.sidedSuccess(isClientSide)
	}

	fun interactWithPart(
		player: Player,
		hand: InteractionHand,
		partIndex: Int?,
		currentAttachment: ScoochwormAttachmentType?
	): InteractionResult {
		val heldStack = player.getItemInHand(hand)

		val growResult = tryGrow(player, heldStack)
		if (growResult != null) return growResult

		if (partIndex == null || currentAttachment == null) return InteractionResult.PASS

		return bodySegments.interact(
			player,
			hand,
			partIndex,
			currentAttachment
		)
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

	// Collision

	override fun canBeCollidedWith(): Boolean = !isDeadOrDying
	override fun canCollideWith(entity: Entity): Boolean {
		return when {
			entity is ScoochwormPartEntity -> entity.parentId != id
			else -> entity is LivingEntity && !entity.isPassenger
		}
	}

	override fun isPushable(): Boolean = false
	override fun isPushedByFluid(type: FluidType): Boolean = false
	override fun getPistonPushReaction(): PushReaction = PushReaction.IGNORE
	override fun knockback(strength: Double, x: Double, z: Double) {}
	override fun push(entity: Entity) {}

	// Entity data

	override fun defineSynchedData(builder: SynchedEntityData.Builder) {
		super.defineSynchedData(builder)
		builder.define(DATA_ATTACHMENT_BOTTOM, Direction.DOWN)
		builder.define(DATA_IS_MOVING, false)
	}

	override fun readAdditionalSaveData(tag: CompoundTag) {
		super.readAdditionalSaveData(tag)

		movementPath.load(tag.getList(PATH_TAG, CompoundTag.TAG_COMPOUND.toInt()))
		bodySegments.load(tag.getList(SEGMENTS_TAG, CompoundTag.TAG_COMPOUND.toInt()))
		isMoving = tag.getBoolean(MOVING_TAG)

		if (!movementPath.isEmpty()) {
			bodySegments.update(movementPath)
		}
	}

	override fun addAdditionalSaveData(tag: CompoundTag) {
		super.addAdditionalSaveData(tag)
		tag.put(PATH_TAG, movementPath.save())
		tag.put(SEGMENTS_TAG, bodySegments.save())
		tag.putBoolean(MOVING_TAG, isMoving)
	}

	// Animation

	override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
	}

	override fun getAnimatableInstanceCache(): AnimatableInstanceCache = animatableInstanceCache

	companion object {
		const val SIZE = 14f / 16f
		const val PART_SPACING = SIZE * 1.2

		private const val HEAD_FOOTSTEP_INDEX = -1
		private const val FOOTSTEP_INTERVAL_TICKS = 3
		private const val FOOTSTEP_CYCLE_PAUSE_TICKS = 40

		private const val SEGMENTS_TAG = "Segments"
		private const val PATH_TAG = "Path"
		private const val MOVING_TAG = "Moving"

		private val DATA_ATTACHMENT_BOTTOM: EntityDataAccessor<Direction> =
			SynchedEntityData.defineId(
				ScoochwormEntity::class.java,
				EntityDataSerializers.DIRECTION
			)

		private val DATA_IS_MOVING: EntityDataAccessor<Boolean> =
			SynchedEntityData.defineId(
				ScoochwormEntity::class.java,
				EntityDataSerializers.BOOLEAN
			)

		fun supportsScoochwormTravel(
			level: Level,
			position: BlockPos,
			attachmentFace: Direction
		): Boolean {
			val block = level.getBlockState(position).block
			if (block !is ScoochwormTravelBlock) return false

			return block.supportsScoochwormTravel(level, attachmentFace)
		}

		fun getMovementYaw(
			travelDirection: Direction,
			bottom: Direction
		): Float {
			if (travelDirection.axis != Direction.Axis.Y) {
				return travelDirection.toYRot()
			}

			val movingUp = travelDirection == Direction.UP

			return when (bottom) {
				Direction.NORTH -> if (movingUp) 180f else 0f
				Direction.SOUTH -> if (movingUp) 0f else 180f
				Direction.WEST -> if (movingUp) 90f else -90f
				Direction.EAST -> if (movingUp) -90f else 90f
				Direction.UP, Direction.DOWN -> 0f
			}
		}

		fun createAttributes(): AttributeSupplier {
			return createMobAttributes()
				.add(Attributes.MAX_HEALTH, 10.0)
				.add(Attributes.MOVEMENT_SPEED, 0.2)
				.build()
		}
	}
}