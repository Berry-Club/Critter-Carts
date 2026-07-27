package dev.aaronhowser.mods.critter_carts.entity

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.util.Mth
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
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

	private val cache = SingletonAnimatableInstanceCache(this)
	private var missingParentTicks = 0
	private var interpolationSteps = 0
	private var interpolationPosition = Vec3.ZERO
	private var interpolationYaw = 0f
	private var interpolationPitch = 0f

	init {
		noPhysics = true
	}

	// Parent

	private val parent: ScoochwormEntity?
		get() = level().getEntity(entityData.get(DATA_PARENT_ID)) as? ScoochwormEntity

	fun attachTo(parent: ScoochwormEntity, partIndex: Int) {
		entityData.set(DATA_PARENT_ID, parent.id)
		entityData.set(DATA_PART_INDEX, partIndex)
	}

	// Movement

	fun moveAlongPath(pathPosition: Vec3, pitch: Float) {
		val movement = pathPosition.subtract(position())
		var movementYaw = yRot

		if (movement.horizontalDistanceSqr() >= MINIMUM_MOVEMENT_SQUARED) {
			movementYaw = Math.toDegrees(
				atan2(movement.z, movement.x)
			).toFloat() - 90f
		}

		setPos(pathPosition)
		yRot = movementYaw
		xRot = pitch
	}

	// Lifecycle

	override fun tick() {
		super.tick()

		if (level().isClientSide) {
			tickInterpolation()
			return
		}

		val parent = parent
		if (parent != null && !parent.isRemoved) {
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
		val parent = parent ?: return false
		return parent.hurt(damageSource, amount)
	}

	override fun canBeCollidedWith(): Boolean = true
	override fun isPickable(): Boolean = true
	override fun isPushable(): Boolean = false

	// Entity data

	override fun defineSynchedData(builder: SynchedEntityData.Builder) {
		builder.define(DATA_PARENT_ID, NO_PARENT)
		builder.define(DATA_PART_INDEX, 0)
	}

	override fun readAdditionalSaveData(tag: CompoundTag) {}
	override fun addAdditionalSaveData(tag: CompoundTag) {}

	override fun shouldBeSaved(): Boolean = false

	// Animation

	override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {}

	override fun getAnimatableInstanceCache(): AnimatableInstanceCache = cache

	companion object {
		private const val NO_PARENT = -1
		private const val MAX_MISSING_PARENT_TICKS = 20
		private const val MINIMUM_MOVEMENT_SQUARED = 0.000001

		private val DATA_PARENT_ID: EntityDataAccessor<Int> =
			SynchedEntityData.defineId(ScoochwormPartEntity::class.java, EntityDataSerializers.INT)

		private val DATA_PART_INDEX: EntityDataAccessor<Int> =
			SynchedEntityData.defineId(ScoochwormPartEntity::class.java, EntityDataSerializers.INT)
	}
}