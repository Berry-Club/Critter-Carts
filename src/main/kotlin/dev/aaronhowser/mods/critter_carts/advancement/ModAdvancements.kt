package dev.aaronhowser.mods.critter_carts.advancement

import dev.aaronhowser.mods.aaron.registry.actual.AaronCriterionTriggers
import dev.aaronhowser.mods.critter_carts.CritterCarts
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player

object ModAdvancements {

	val ROOT = guide("root")
	val BREAK_APPLE_SLICE = guide("break_apple_slice")
	val INTERACT_WITH_SCOOCHWORM = guide("interact_with_scoochworm")
	val ATTACH_TO_SCOOCHWORM = guide("attach_to_scoochworm")
	val SPLIT_SCOOCHWORM = guide("split_scoochworm")
	val WITNESS_HEAD_ON_COLLISION = guide("witness_head_on_collision")
	val DYE_SCOOCHWORM = guide("dye_scoochworm")
	val EAT_DYEBERRY = guide("eat_dyeberry")
	val EAT_AARONBERRY = guide("eat_aaronberry")

	fun trigger(player: Player, action: ResourceLocation) {
		if (player !is ServerPlayer) return

		AaronCriterionTriggers.PLAYER_ACTION.get().trigger(player, action)
	}

	private fun guide(path: String): ResourceLocation {
		return CritterCarts.modResource("guide/$path")
	}
}