package dev.aaronhowser.mods.critter_carts.entity

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache
import software.bernie.geckolib.animation.AnimatableManager

class ScoochwormPartEntity(
	entityType: EntityType<ScoochwormPartEntity>,
	level: Level
) : Entity(entityType, level), GeoEntity {

	private val cache = SingletonAnimatableInstanceCache(this)
	private var missingParentTicks = 0

	init {
		noPhysics = true
	}

	fun attachTo(parent: ScoochwormEntity, partIndex: Int) {
		entityData.set(DATA_PARENT_ID, parent.id)
		entityData.set(DATA_PART_INDEX, partIndex)
	}

	fun moveAlongPath(pathPosition: Vec3, pitch: Float) {
		val movement = pathPosition.subtract(position())
		var movementRotation = yRot

		if (movement.horizontalDistanceSqr() >= MINIMUM_MOVEMENT_SQUARED) {
			movementRotation = Math.toDegrees(
				kotlin.math.atan2(-movement.x, movement.z)
			).toFloat()
		}

		moveTo(
			pathPosition.x,
			pathPosition.y,
			pathPosition.z,
			movementRotation,
			pitch
		)
	}

	override fun tick() {
		super.tick()

		if (level().isClientSide) return

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

	override fun hurt(damageSource: DamageSource, amount: Float): Boolean {
		val parent = parent ?: return false
		return parent.hurt(damageSource, amount)
	}

	override fun defineSynchedData(builder: SynchedEntityData.Builder) {
		builder.define(DATA_PARENT_ID, NO_PARENT)
		builder.define(DATA_PART_INDEX, 0)
	}

	override fun readAdditionalSaveData(tag: CompoundTag) {
	}

	override fun addAdditionalSaveData(tag: CompoundTag) {
	}

	override fun shouldBeSaved(): Boolean = false
	override fun isPickable(): Boolean = true
	override fun isPushable(): Boolean = false
	override fun canBeCollidedWith(): Boolean = true

	override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
	}

	override fun getAnimatableInstanceCache(): AnimatableInstanceCache = cache

	private val parent: ScoochwormEntity?
		get() = level().getEntity(entityData.get(DATA_PARENT_ID)) as? ScoochwormEntity

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