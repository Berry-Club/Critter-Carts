package dev.aaronhowser.mods.critter_carts.entity.goal

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isItem
import dev.aaronhowser.mods.critter_carts.entity.ScoochwormEntity
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Items
import java.util.*

class ScoochwormLookAtMelonGoal(
	private val scoochworm: ScoochwormEntity
) : Goal() {

	private var player: Player? = null

	init {
		flags = EnumSet.of(Flag.LOOK)
	}

	override fun canUse(): Boolean {
		player = findNearestMelonHolder()
		return player != null
	}

	override fun canContinueToUse(): Boolean {
		val player = this.player ?: return false

		return player.isAlive
			&& player.isHoldingMelon()
			&& scoochworm.distanceToSqr(player) <= LOOK_DISTANCE_SQUARED
	}

	override fun tick() {
		val player = this.player ?: return
		scoochworm.lookControl.setLookAt(player, 10f, 40f)
	}

	override fun stop() {
		player = null
	}

	private fun findNearestMelonHolder(): Player? {
		val nearbyPlayers = scoochworm.level().getEntitiesOfClass(
			Player::class.java,
			scoochworm.boundingBox.inflate(LOOK_DISTANCE)
		)

		var nearestPlayer: Player? = null
		var nearestDistanceSquared = LOOK_DISTANCE_SQUARED

		for (player in nearbyPlayers) {
			if (player.isSpectator || !player.isAlive || !player.isHoldingMelon()) continue

			val distanceSquared = scoochworm.distanceToSqr(player)
			if (distanceSquared > nearestDistanceSquared) continue

			nearestPlayer = player
			nearestDistanceSquared = distanceSquared
		}

		return nearestPlayer
	}

	private fun Player.isHoldingMelon(): Boolean {
		return mainHandItem.isItem(Items.MELON)
			|| offhandItem.isItem(Items.MELON)
	}

	companion object {
		private const val LOOK_DISTANCE = 6.0
		private const val LOOK_DISTANCE_SQUARED = LOOK_DISTANCE * LOOK_DISTANCE
	}

}