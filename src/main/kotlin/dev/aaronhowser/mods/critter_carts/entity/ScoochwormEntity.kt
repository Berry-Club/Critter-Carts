package dev.aaronhowser.mods.critter_carts.entity

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isBlock
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isClientSide
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isItem
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isServerSide
import dev.aaronhowser.mods.aaron.misc.AaronExtensions.nextRange
import dev.aaronhowser.mods.critter_carts.block.ScoochwormTravelBlock
import dev.aaronhowser.mods.critter_carts.datagen.tag.ModBlockTagsProvider
import dev.aaronhowser.mods.critter_carts.entity.control.ScoochwormMoveControl
import dev.aaronhowser.mods.critter_carts.entity.data.ScoochwormPath
import dev.aaronhowser.mods.critter_carts.entity.data.ScoochwormSegments
import dev.aaronhowser.mods.critter_carts.entity.data.attachment.ScoochwormAttachmentType
import dev.aaronhowser.mods.critter_carts.entity.goal.ScoochwormTravelGoal
import dev.aaronhowser.mods.critter_carts.entity.goal.ScoochwormWanderGoal
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
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.item.ItemEntity
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
import java.util.*

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
	var surfaceTravelDirection: Vec3? = null
	var isTraversingSurfaceCorner = false

	init {
		moveControl = scoochwormMoveControl
	}

	var supportDirection: Direction
		get() = entityData.get(DATA_SUPPORT_DIRECTION)
		set(value) = entityData.set(DATA_SUPPORT_DIRECTION, value)

	var supportPosition: BlockPos?
		get() = entityData.get(DATA_SUPPORT_POSITION).orElse(null)
		private set(value) = entityData.set(DATA_SUPPORT_POSITION, Optional.ofNullable(value))

	var isTryingToMove: Boolean
		get() = entityData.get(DATA_IS_TRYING_TO_MOVE)
		private set(value) = entityData.set(DATA_IS_TRYING_TO_MOVE, value)

	override fun registerGoals() {
		goalSelector.addGoal(0, ScoochwormTravelGoal(this))
		goalSelector.addGoal(1, ScoochwormWanderGoal(this))
	}

	override fun aiStep() {
		if (isServerSide && !hasValidSupport()) {
			supportPosition = null
		}

		isNoGravity = supportPosition != null

		super.aiStep()
		bodySegments.tick()

		if (isServerSide) {
			eatTouchingItems()
		}

		if (isClientSide || !isTryingToMove) return

		// Record the path that the head has traveled,
		// and then set each segment to be a set distance from the head along that path
		movementPath.record(position(), supportDirection, yRot)
		bodySegments.update(movementPath)

		playNextFootstep()
	}

	private fun eatTouchingItems() {
		val touchingItems = level().getEntitiesOfClass(
			ItemEntity::class.java,
			boundingBox
		)

		for (itemEntity in touchingItems) {
			if (itemEntity.hasPickUpDelay()) continue

			val itemStack = itemEntity.item
			val remainder = bodySegments.insertIntoWickerBaskets(itemStack)
			if (remainder.count == itemStack.count) continue

			if (remainder.isEmpty) {
				itemEntity.discard()
			} else {
				itemEntity.item = remainder
			}

			playSound(SoundEvents.GENERIC_EAT, 1f, 1f)
			gameEvent(GameEvent.EAT)
		}
	}

	fun hasValidSupport(): Boolean {
		val currentSupportPosition = supportPosition ?: return false

		return supportsScoochwormTravel(level(), currentSupportPosition, supportDirection.opposite)
			|| supportsFreeTravel(level(), currentSupportPosition, supportDirection.opposite)
	}

	fun attachToSupport(position: BlockPos, direction: Direction) {
		supportDirection = direction
		supportPosition = position.immutable()
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
			isTryingToMove = !isTryingToMove
			if (!isTryingToMove) deltaMovement = Vec3.ZERO
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

	override fun canCollideWith(entity: Entity): Boolean {
		return when (entity) {
			is ScoochwormEntity -> entity.id != id
			is ScoochwormPartEntity -> entity.parentId != id
			else -> super.canCollideWith(entity)
		}
	}

	override fun isPushable(): Boolean = false
	override fun isInWall(): Boolean = !isTraversingSurfaceCorner && super.isInWall()
	override fun isPushedByFluid(type: FluidType): Boolean = false
	override fun getPistonPushReaction(): PushReaction = PushReaction.IGNORE
	override fun knockback(strength: Double, x: Double, z: Double) {}
	override fun push(entity: Entity) {}
	override fun doPush(entity: Entity) {
		if (entity is ItemEntity) return
		super.doPush(entity)
	}

	// Entity data

	override fun defineSynchedData(builder: SynchedEntityData.Builder) {
		super.defineSynchedData(builder)
		builder.define(DATA_SUPPORT_DIRECTION, Direction.DOWN)
		builder.define(DATA_SUPPORT_POSITION, Optional.empty())
		builder.define(DATA_IS_TRYING_TO_MOVE, false)
	}

	override fun readAdditionalSaveData(tag: CompoundTag) {
		super.readAdditionalSaveData(tag)

		movementPath.load(tag.getList(PATH_TAG, CompoundTag.TAG_COMPOUND.toInt()))
		bodySegments.load(tag.getList(SEGMENTS_TAG, CompoundTag.TAG_COMPOUND.toInt()))
		isTryingToMove = tag.getBoolean(TRYING_TO_MOVE_TAG)

		supportDirection = Direction.from3DDataValue(tag.getInt(SUPPORT_DIRECTION_TAG))
		supportPosition = if (tag.contains(SUPPORT_POSITION_TAG)) {
			BlockPos.of(tag.getLong(SUPPORT_POSITION_TAG))
		} else {
			null
		}

		if (!movementPath.isEmpty()) {
			bodySegments.update(movementPath)
		}
	}

	override fun addAdditionalSaveData(tag: CompoundTag) {
		super.addAdditionalSaveData(tag)
		tag.put(PATH_TAG, movementPath.save())
		tag.put(SEGMENTS_TAG, bodySegments.save())
		tag.putBoolean(TRYING_TO_MOVE_TAG, isTryingToMove)
		tag.putInt(SUPPORT_DIRECTION_TAG, supportDirection.get3DDataValue())

		val currentSupportPosition = supportPosition
		if (currentSupportPosition != null) {
			tag.putLong(SUPPORT_POSITION_TAG, currentSupportPosition.asLong())
		}
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
		private const val TRYING_TO_MOVE_TAG = "Moving"
		private const val SUPPORT_DIRECTION_TAG = "AttachmentBottom"
		private const val SUPPORT_POSITION_TAG = "AttachmentPosition"

		private val DATA_SUPPORT_DIRECTION: EntityDataAccessor<Direction> =
			SynchedEntityData.defineId(
				ScoochwormEntity::class.java,
				EntityDataSerializers.DIRECTION
			)

		private val DATA_SUPPORT_POSITION: EntityDataAccessor<Optional<BlockPos>> =
			SynchedEntityData.defineId(
				ScoochwormEntity::class.java,
				EntityDataSerializers.OPTIONAL_BLOCK_POS
			)

		private val DATA_IS_TRYING_TO_MOVE: EntityDataAccessor<Boolean> =
			SynchedEntityData.defineId(
				ScoochwormEntity::class.java,
				EntityDataSerializers.BOOLEAN
			)

		fun supportsScoochwormTravel(
			level: Level,
			position: BlockPos,
			attachmentFace: Direction
		): Boolean {
			val blockState = level.getBlockState(position)
			if (!blockState.isBlock(ModBlockTagsProvider.SUPPORTS_SCOOCHWORM_TRAVEL)) return false

			val block = blockState.block
			return if (block is ScoochwormTravelBlock) {
				block.supportsScoochwormTravel(blockState, level, position, attachmentFace)
			} else {
				true
			}
		}

		fun supportsFreeTravel(
			level: Level,
			position: BlockPos,
			attachmentFace: Direction
		): Boolean {
			val blockState = level.getBlockState(position)
			if (blockState.isBlock(ModBlockTagsProvider.SUPPORTS_SCOOCHWORM_TRAVEL)) return false

			return blockState.isFaceSturdy(level, position, attachmentFace)
		}

		fun getMovementYaw(
			travelDirection: Direction,
			supportDirection: Direction
		): Float {
			if (travelDirection.axis != Direction.Axis.Y) {
				return travelDirection.toYRot()
			}

			val movingUp = travelDirection == Direction.UP

			return when (supportDirection) {
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