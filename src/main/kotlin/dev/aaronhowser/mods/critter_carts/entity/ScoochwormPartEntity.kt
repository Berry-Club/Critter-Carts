package dev.aaronhowser.mods.critter_carts.entity

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isClientSide
import dev.aaronhowser.mods.critter_carts.entity.data.ScoochwormPartAttachment
import dev.aaronhowser.mods.critter_carts.registry.ModEntityDataSerializers
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache
import software.bernie.geckolib.animation.AnimatableManager
import kotlin.math.atan2

class ScoochwormPartEntity(
	entityType: EntityType<ScoochwormPartEntity>,
	level: Level
) : Entity(entityType, level), GeoEntity {

	private val animatableInstanceCache = SingletonAnimatableInstanceCache(this)
	private var missingParentTicks = 0
	private var lerpSteps = 0
	private var lerpX = 0.0
	private var lerpY = 0.0
	private var lerpZ = 0.0
	private var lerpYRot = 0.0
	private var lerpXRot = 0.0

	init {
		noPhysics = true
	}

	// Parent

	var parentId: Int
		get() = entityData.get(DATA_PARENT_ID)
		set(value) = entityData.set(DATA_PARENT_ID, value)

	private fun getParent(): ScoochwormEntity? {
		return level().getEntity(parentId) as? ScoochwormEntity
	}

	var partIndex: Int
		get() = entityData.get(DATA_PART_INDEX)
		set(value) = entityData.set(DATA_PART_INDEX, value)

	var attachment: ScoochwormPartAttachment
		get() = entityData.get(DATA_ATTACHMENT)
		set(value) = entityData.set(DATA_ATTACHMENT, value)

	private var attachmentBottom: Direction
		get() = entityData.get(DATA_ATTACHMENT_BOTTOM)
		set(value) = entityData.set(DATA_ATTACHMENT_BOTTOM, value)

	fun attachTo(
		parentEntity: ScoochwormEntity,
		partIndex: Int,
		attachment: ScoochwormPartAttachment
	) {
		this.parentId = parentEntity.id
		this.partIndex = partIndex
		this.attachment = attachment
	}

	// Movement

	fun moveAlongPath(pathPosition: Vec3, bottom: Direction) {
		val displacement = position().vectorTo(pathPosition)
		var movementYaw = yRot

		if (displacement.horizontalDistanceSqr() >= MINIMUM_MOVEMENT_DISTANCE_SQUARED) {
			movementYaw = Math.toDegrees(
				atan2(displacement.z, displacement.x)
			).toFloat() - 90f
		}

		setPos(pathPosition)
		attachmentBottom = bottom
		yRot = movementYaw
		xRot = when (bottom) {
			Direction.UP -> 180f
			Direction.DOWN -> 0f
			else -> -bottom.toYRot()
		}
	}

	override fun getPassengerAttachmentPoint(
		entity: Entity,
		dimensions: EntityDimensions,
		partialTick: Float
	): Vec3 {
		val attachmentDistance = super.getPassengerAttachmentPoint(
			entity,
			dimensions,
			partialTick
		).y

		val attachmentDirection = attachmentBottom.opposite

		return Vec3(
			attachmentDirection.stepX * attachmentDistance,
			attachmentDirection.stepY * attachmentDistance,
			attachmentDirection.stepZ * attachmentDistance
		)
	}

	// Lifecycle

	override fun tick() {
		super.tick()

		if (isClientSide) {
			tickInterpolation()
			return
		}

		val parentEntity = getParent()
		if (parentEntity != null && !parentEntity.isRemoved) {
			missingParentTicks = 0
			return
		}

		missingParentTicks++
		if (missingParentTicks > MAX_MISSING_PARENT_TICKS) {
			discard()
		}
	}

	// Interpolation

	override fun lerpTo(
		x: Double,
		y: Double,
		z: Double,
		yRot: Float,
		xRot: Float,
		lerpSteps: Int
	) {
		lerpX = x
		lerpY = y
		lerpZ = z
		lerpYRot = yRot.toDouble()
		lerpXRot = xRot.toDouble()
		this.lerpSteps = lerpSteps
	}

	private fun tickInterpolation() {
		if (lerpSteps <= 0) return

		val x = x + (lerpX - x) / lerpSteps
		val y = y + (lerpY - y) / lerpSteps
		val z = z + (lerpZ - z) / lerpSteps

		yRot += Mth.wrapDegrees(lerpYRot - yRot).toFloat() / lerpSteps
		xRot += ((lerpXRot - xRot) / lerpSteps).toFloat()

		lerpSteps--
		setPos(x, y, z)
	}

	// Collision

	override fun hurt(damageSource: DamageSource, amount: Float): Boolean {
		val parentEntity = getParent() ?: return false
		return parentEntity.hurt(damageSource, amount)
	}

	override fun interact(player: Player, hand: InteractionHand): InteractionResult {
		val parentEntity = getParent() ?: return InteractionResult.PASS
		return parentEntity.interactWithPart(player, hand, partIndex, attachment)
	}

	override fun canBeCollidedWith(): Boolean = true
	override fun canCollideWith(entity: Entity): Boolean {
		return when (entity) {
			is ScoochwormEntity -> {
				entity.id != parentId
			}

			is ScoochwormPartEntity -> {
				entity.parentId != parentId
			}

			else -> !entity.isPassenger
		}
	}

	override fun isPickable(): Boolean = true
	override fun isPushable(): Boolean = false

	// Entity data

	override fun defineSynchedData(builder: SynchedEntityData.Builder) {
		builder.define(DATA_PARENT_ID, NO_PARENT)
		builder.define(DATA_PART_INDEX, 0)
		builder.define(DATA_ATTACHMENT, ScoochwormPartAttachment.NONE)
		builder.define(DATA_ATTACHMENT_BOTTOM, Direction.DOWN)
	}

	override fun readAdditionalSaveData(tag: CompoundTag) {}
	override fun addAdditionalSaveData(tag: CompoundTag) {}

	override fun shouldBeSaved(): Boolean = false

	// Animation

	override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {}

	override fun getAnimatableInstanceCache(): AnimatableInstanceCache = animatableInstanceCache

	companion object {
		private const val NO_PARENT = -1
		private const val MAX_MISSING_PARENT_TICKS = 20
		private const val MINIMUM_MOVEMENT_DISTANCE_SQUARED = 0.000001

		private val DATA_PARENT_ID: EntityDataAccessor<Int> =
			SynchedEntityData.defineId(ScoochwormPartEntity::class.java, EntityDataSerializers.INT)

		private val DATA_PART_INDEX: EntityDataAccessor<Int> =
			SynchedEntityData.defineId(ScoochwormPartEntity::class.java, EntityDataSerializers.INT)

		private val DATA_ATTACHMENT: EntityDataAccessor<ScoochwormPartAttachment> =
			SynchedEntityData.defineId(
				ScoochwormPartEntity::class.java,
				ModEntityDataSerializers.SCOOCHWORM_PART_ATTACHMENT.get()
			)

		private val DATA_ATTACHMENT_BOTTOM: EntityDataAccessor<Direction> =
			SynchedEntityData.defineId(
				ScoochwormPartEntity::class.java,
				EntityDataSerializers.DIRECTION
			)
	}

}