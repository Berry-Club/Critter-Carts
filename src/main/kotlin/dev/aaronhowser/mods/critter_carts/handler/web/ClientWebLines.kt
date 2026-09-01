package dev.aaronhowser.mods.critter_carts.handler.web

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isItem
import dev.aaronhowser.mods.critter_carts.datagen.tag.ModItemTagsProvider
import dev.aaronhowser.mods.critter_carts.handler.web.node.WebNode
import dev.aaronhowser.mods.critter_carts.packet.client_to_server.WebLineInteractPacket
import dev.aaronhowser.mods.critter_carts.registry.ModItems
import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.client.event.InputEvent
import java.util.*

object ClientWebLines {

	private val lines: MutableMap<UUID, WebLine> = mutableMapOf()
	private val nodes: MutableMap<UUID, WebNode> = mutableMapOf()

	fun getLines(): List<WebLine> {
		return lines.values.toList()
	}

	fun addLines(newNodes: List<WebNode>, newLines: List<WebLineData>) {
		for (node in newNodes) {
			if (node.uuid !in nodes) {
				nodes[node.uuid] = node
			}
		}

		for (lineData in newLines) {
			val firstNode = nodes[lineData.firstNodeUuid] ?: continue
			val secondNode = nodes[lineData.secondNodeUuid] ?: continue
			lines[lineData.uuid] = WebLine(lineData.uuid, firstNode, secondNode)
		}
	}

	fun removeLine(uuid: UUID) {
		lines.remove(uuid)
	}

	fun clear() {
		lines.clear()
		nodes.clear()
	}

	fun handleInteractionEvent(event: InputEvent.InteractionKeyMappingTriggered) {
		if (event.isUseItem) {
			rightClickLine(event)
		}
	}

	private fun rightClickLine(event: InputEvent.InteractionKeyMappingTriggered) {
		val player = Minecraft.getInstance().player ?: return
		val interactionHand = getInteractionHand(player) ?: return

		val targetedNode = getLookedAtNode(
			player,
			player.eyePosition,
			player.lookAngle,
			interactionHand
		) ?: return

		val hand = event.hand
		if (hand == interactionHand) {
			val targetsNode = targetedNode.lineUuid == null
			val targetUuid = targetedNode.lineUuid ?: targetedNode.node.uuid
			WebLineInteractPacket(
				targetUuid,
				targetsNode,
				targetedNode.node.position,
				hand
			).messageServer()
		}

		event.isCanceled = true
	}

	private fun getInteractionHand(player: Player): InteractionHand? {
		if (player.mainHandItem.isItem(ModItemTagsProvider.WEB_LINE_INTERACTABLE)) {
			return InteractionHand.MAIN_HAND
		}

		if (player.offhandItem.isItem(ModItemTagsProvider.WEB_LINE_INTERACTABLE)) {
			return InteractionHand.OFF_HAND
		}

		return null
	}

	fun getHoveredAnchor(
		player: Player,
		eyePosition: Vec3,
		viewVector: Vec3
	): TargetedWebNode? {
		if (!player.mainHandItem.isItem(ModItemTagsProvider.WEB_LINE_INTERACTABLE)
			&& !player.offhandItem.isItem(ModItemTagsProvider.WEB_LINE_INTERACTABLE)
		) return null

		val interactionHand = getInteractionHand(player) ?: return null
		return getLookedAtNode(player, eyePosition, viewVector, interactionHand)
	}

	private fun getLookedAtNode(
		player: Player,
		eyePosition: Vec3,
		viewVector: Vec3,
		interactionHand: InteractionHand
	): TargetedWebNode? {
		val interactionRange = player.blockInteractionRange()
		val lookOffset = viewVector.scale(interactionRange)
		val lookEnd = eyePosition.add(lookOffset)
		val itemStack = player.getItemInHand(interactionHand)
		val snapToExistingNode = itemStack.isItem(ModItems.WEB_FLUID)

		return WebLineInteractionHandler.getTargetedNode(
			lines.values.toList(),
			eyePosition,
			lookEnd,
			snapToExistingNode
		)
	}

}