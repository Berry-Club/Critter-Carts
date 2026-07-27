package dev.aaronhowser.mods.critter_carts.entity

import dev.aaronhowser.mods.critter_carts.entity.control.ScoochwormMoveControl
import dev.aaronhowser.mods.critter_carts.entity.goal.ScoochstemFollowGoal
import dev.aaronhowser.mods.critter_carts.registry.ModEntityTypes
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.level.Level
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

	private val cache = SingletonAnimatableInstanceCache(this)
	private val scoochwormMoveControl = ScoochwormMoveControl(this)
	private val bodyParts: MutableList<ScoochwormPartEntity> = mutableListOf()
	private val pathHistory: ArrayDeque<Vec3> = ArrayDeque()

	init {
		moveControl = scoochwormMoveControl
	}

	override fun registerGoals() {
		goalSelector.addGoal(0, ScoochstemFollowGoal(this))
	}

	override fun aiStep() {
		super.aiStep()

		if (level().isClientSide) return

		recordPath()
		ensureBodyParts()
		updateBodyParts()
	}

	override fun remove(reason: RemovalReason) {
		super.remove(reason)

		for (bodyPart in bodyParts) {
			bodyPart.discard()
		}

		bodyParts.clear()
	}

	override fun canBeCollidedWith(): Boolean = !isDeadOrDying
	override fun isPushable(): Boolean = false
	override fun isPushedByFluid(type: FluidType): Boolean = false
	override fun getPistonPushReaction(): PushReaction = PushReaction.IGNORE
	override fun knockback(strength: Double, x: Double, z: Double) {}
	override fun push(entity: Entity) {}

	// Animation stuff

	override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
	}

	override fun getAnimatableInstanceCache(): AnimatableInstanceCache = cache

	private fun recordPath() {
		val currentPosition = position()

		if (pathHistory.isEmpty()) {
			val forward = Vec3.directionFromRotation(0f, yRot)
			val fullBodyLength = getPartDistance(BODY_PART_COUNT - 1)

			pathHistory.addFirst(currentPosition)
			pathHistory.addLast(
				currentPosition.subtract(forward.scale(fullBodyLength))
			)
			return
		}

		val previousPosition = pathHistory.peekFirst()

		if (previousPosition.distanceToSqr(currentPosition) > MINIMUM_PATH_STEP_SQUARED) {
			pathHistory.addFirst(currentPosition)
		}

		while (pathHistory.size > MAX_PATH_POINTS) {
			pathHistory.removeLast()
		}
	}

	private fun ensureBodyParts() {
		bodyParts.removeAll { it.isRemoved }

		while (bodyParts.size < BODY_PART_COUNT) {
			val partIndex = bodyParts.size
			val bodyPart = ScoochwormPartEntity(
				ModEntityTypes.SCOOCHWORM_PART.get(),
				level()
			)
			val pathPosition = getPathPosition(getPartDistance(partIndex))

			bodyPart.attachTo(this, partIndex)
			bodyPart.moveTo(
				pathPosition.x,
				pathPosition.y,
				pathPosition.z,
				yRot,
				xRot
			)
			level().addFreshEntity(bodyPart)
			bodyParts.add(bodyPart)
		}
	}

	private fun updateBodyParts() {
		for (index in bodyParts.indices) {
			val distance = getPartDistance(index)
			val pathPosition = getPathPosition(distance)
			val bodyPart = bodyParts[index]

			bodyPart.moveAlongPath(pathPosition, xRot)
		}
	}

	private fun getPathPosition(targetDistance: Double): Vec3 {
		var remainingDistance = targetDistance
		var newerPosition = pathHistory.peekFirst() ?: position()

		for (olderPosition in pathHistory.drop(1)) {
			val sectionDistance = newerPosition.distanceTo(olderPosition)

			if (sectionDistance >= remainingDistance && sectionDistance > 0.0) {
				return newerPosition.lerp(olderPosition, remainingDistance / sectionDistance)
			}

			remainingDistance -= sectionDistance
			newerPosition = olderPosition
		}

		return newerPosition
	}

	private fun getPartDistance(partIndex: Int): Double {
		return PART_SPACING * (partIndex + 1)
	}

	companion object {
		const val SIZE = 14f / 16f
		const val BODY_PART_COUNT = 4
		const val PART_SPACING = SIZE * 1.2

		private const val MAX_PATH_POINTS = 256
		private const val MINIMUM_PATH_STEP_SQUARED = 0.000001

		fun createAttributes(): AttributeSupplier {
			return createMobAttributes()
				.add(Attributes.MAX_HEALTH, 10.0)
				.add(Attributes.MOVEMENT_SPEED, 0.2)
				.build()
		}
	}
}