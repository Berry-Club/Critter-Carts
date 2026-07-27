package dev.aaronhowser.mods.critter_carts.entity

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isClientSide
import dev.aaronhowser.mods.critter_carts.registry.ModEntityDataSerializers
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
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
	private var interpolationSteps = 0
	private var interpolationPosition = Vec3.ZERO
	private var interpolationYaw = 0f
	private var interpolationPitch = 0f

	init {
		noPhysics = true
	}

	// Parent

	private fun getParent(): ScoochwormEntity? {
		return level().getEntity(entityData.get(DATA_PARENT_ID)) as? ScoochwormEntity
	}

	val attachment: ScoochwormPartAttachment
		get() = entityData.get(DATA_ATTACHMENT)

	fun attachTo(
		parentEntity: ScoochwormEntity,
		partIndex: Int,
		attachment: ScoochwormPartAttachment
	) {
		entityData.set(DATA_PARENT_ID, parentEntity.id)
		entityData.set(DATA_PART_INDEX, partIndex)
		setAttachment(attachment)
	}

	fun setAttachment(attachment: ScoochwormPartAttachment) {
		entityData.set(DATA_ATTACHMENT, attachment)
	}

	// Movement

	fun moveAlongPath(pathPosition: Vec3, pitch: Float) {
		val displacement = position().vectorTo(pathPosition)
		var movementYaw = yRot

		if (displacement.horizontalDistanceSqr() >= MINIMUM_MOVEMENT_DISTANCE_SQUARED) {
			movementYaw = Math.toDegrees(
				atan2(displacement.z, displacement.x)
			).toFloat() - 90f
		}

		setPos(pathPosition)
		yRot = movementYaw
		xRot = pitch
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
		interpolationPosition = Vec3(x, y, z)
		interpolationYaw = yRot
		interpolationPitch = xRot
		interpolationSteps = lerpSteps
	}

	private fun tickInterpolation() {
		if (interpolationSteps <= 0) return

		val progress = 1.0 / interpolationSteps
		setPos(position().lerp(interpolationPosition, progress))
		yRot += Mth.wrapDegrees(interpolationYaw - yRot) / interpolationSteps
		xRot += (interpolationPitch - xRot) / interpolationSteps
		interpolationSteps--
	}

	// Collision

	override fun hurt(damageSource: DamageSource, amount: Float): Boolean {
		val parentEntity = getParent() ?: return false
		return parentEntity.hurt(damageSource, amount)
	}

	override fun interact(player: Player, hand: InteractionHand): InteractionResult {
		val parentEntity = getParent() ?: return InteractionResult.PASS
		return parentEntity.interactWithPart(player, hand, entityData.get(DATA_PART_INDEX))
	}

	override fun canBeCollidedWith(): Boolean = true
	override fun isPickable(): Boolean = true
	override fun isPushable(): Boolean = false

	// Entity data

	override fun defineSynchedData(builder: SynchedEntityData.Builder) {
		builder.define(DATA_PARENT_ID, NO_PARENT)
		builder.define(DATA_PART_INDEX, 0)
		builder.define(DATA_ATTACHMENT, ScoochwormPartAttachment.NONE)
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
	}

}