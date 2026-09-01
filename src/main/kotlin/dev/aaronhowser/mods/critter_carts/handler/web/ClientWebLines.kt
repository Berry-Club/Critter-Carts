package dev.aaronhowser.mods.critter_carts.handler.web

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isItem
import dev.aaronhowser.mods.critter_carts.datagen.tag.ModItemTagsProvider
import dev.aaronhowser.mods.critter_carts.handler.web.node.LineAnchor
import dev.aaronhowser.mods.critter_carts.packet.client_to_server.WebLineInteractPacket
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import java.util.UUID

object ClientWebLines {

	private val lines: MutableMap<UUID, WebLine> = mutableMapOf()

	fun getLines(): List<WebLine> {
		return lines.values.toList()
	}

	fun addLines(newLines: List<WebLine>) {
		for (line in newLines) {
			lines[line.uuid] = line
		}
	}

	fun removeLine(uuid: UUID) {
		lines.remove(uuid)
	}

	fun clear() {
		lines.clear()
	}

	fun interact(player: Player, hand: InteractionHand): Boolean {
		if (!player.level().isClientSide) return false

		val itemStack = player.getItemInHand(hand)
		if (!itemStack.isItem(ModItemTagsProvider.WEB_LINE_INTERACTABLE)) return false

		val lineAnchor = getLookedAtAnchor(player) ?: return false
		WebLineInteractPacket(lineAnchor.lineUuid, lineAnchor.position, hand).messageServer()
		return true
	}

	fun getHoveredAnchor(player: Player): LineAnchor? {
		if (!player.mainHandItem.isItem(ModItemTagsProvider.WEB_LINE_INTERACTABLE)
			&& !player.offhandItem.isItem(ModItemTagsProvider.WEB_LINE_INTERACTABLE)
		) return null

		return getLookedAtAnchor(player)
	}

	private fun getLookedAtAnchor(player: Player): LineAnchor? {
		val eyePosition = player.eyePosition
		val interactionRange = player.blockInteractionRange()
		val lookOffset = player.lookAngle.scale(interactionRange)
		val lookEnd = eyePosition.add(lookOffset)

		return WebLineInteractionHandler.getTargetedLineAnchor(
			lines.values.toList(),
			eyePosition,
			lookEnd
		)
	}

}