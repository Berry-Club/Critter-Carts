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

	fun getLines(): Collection<WebLine> {
		return lines.values
	}

	fun addLines(newLines: Collection<WebLine>) {
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
		val lookEnd = player.eyePosition.add(player.lookAngle.scale(player.blockInteractionRange()))
		return WebLineInteractionHandler.findLineAnchor(lines.values, player.eyePosition, lookEnd)
	}

}