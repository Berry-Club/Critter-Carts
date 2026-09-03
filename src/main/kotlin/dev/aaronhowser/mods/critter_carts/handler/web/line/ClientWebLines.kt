package dev.aaronhowser.mods.critter_carts.handler.web.line

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isItem
import dev.aaronhowser.mods.critter_carts.datagen.tag.ModItemTagsProvider
import dev.aaronhowser.mods.critter_carts.handler.web.TargetedWebNode
import dev.aaronhowser.mods.critter_carts.handler.web.WebLineInteractionHandler
import dev.aaronhowser.mods.critter_carts.handler.web.node.WebBlockAnchor
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

	fun getNodes(): List<WebNode> {
		return nodes.values.toList()
	}

	fun addLines(newNodes: List<WebNode>, newLines: List<WebLineData>) {
		for (node in newNodes) {
			nodes[node.uuid] = node
		}

		for (lineData in newLines) {
			val firstNode = nodes[lineData.firstNodeUuid] ?: continue
			val secondNode = nodes[lineData.secondNodeUuid] ?: continue
			val line = WebLine(lineData.uuid, firstNode, secondNode)
			val previousLine = lines.put(line.uuid, line)
			if (previousLine != null) {
				detachLine(previousLine)
			}

			firstNode.addLine(line)
			secondNode.addLine(line)
		}
	}

	fun removeLine(uuid: UUID) {
		val line = lines.remove(uuid) ?: return
		detachLine(line)
		removeNodeIfOrphaned(line.firstNode)
		removeNodeIfOrphaned(line.secondNode)
	}

	fun clear() {
		for (line in lines.values) {
			detachLine(line)
		}

		lines.clear()
		nodes.clear()
	}

	private fun detachLine(line: WebLine) {
		line.firstNode.removeLine(line)
		line.secondNode.removeLine(line)
	}

	private fun removeNodeIfOrphaned(node: WebNode) {
		if (node.lines.isNotEmpty()) return
		if (nodes[node.uuid] !== node) return

		nodes.remove(node.uuid)
	}

	fun handleInteractionEvent(event: InputEvent.InteractionKeyMappingTriggered) {
		if (event.isUseItem) {
			rightClickLine(event)
		}
	}

	private fun rightClickLine(event: InputEvent.InteractionKeyMappingTriggered) {
		val player = Minecraft.getInstance().player ?: return
		val interactionHand = getInteractionHand(player)
			?: event.hand

		val targetedNode = getLookedAtNode(
			player,
			player.eyePosition,
			player.lookAngle,
			interactionHand
		) ?: return
		val heldStack = player.getItemInHand(interactionHand)
		if (!heldStack.isItem(ModItemTagsProvider.WEB_LINE_INTERACTABLE)) {
			val blockAnchor = targetedNode.node as? WebBlockAnchor ?: return
			if (!blockAnchor.hasNestInterface || targetedNode.lineUuid != null) return
		}

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
		val interactionHand = getInteractionHand(player) ?: InteractionHand.MAIN_HAND
		val targetedNode = getLookedAtNode(player, eyePosition, viewVector, interactionHand) ?: return null
		if (player.getItemInHand(interactionHand).isItem(ModItemTagsProvider.WEB_LINE_INTERACTABLE)) {
			return targetedNode
		}

		val blockAnchor = targetedNode.node as? WebBlockAnchor ?: return null
		if (!blockAnchor.hasNestInterface || targetedNode.lineUuid != null) return null
		return targetedNode
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
		val installedInterface = WebLineInteractionHandler.getTargetedNode(
			lines.values.toList(),
			eyePosition,
			lookEnd,
			true,
			true
		)
		val interfaceAnchor = installedInterface?.node as? WebBlockAnchor
		if (interfaceAnchor?.hasNestInterface == true) return installedInterface

		val snapToExistingNode = itemStack.isItem(ModItems.WEB_FLUID)
			|| itemStack.isItem(ModItems.WEB_PATHFINDER)
			|| itemStack.isItem(ModItems.SPIDER_NEST_INTERFACE)
			|| !itemStack.isItem(ModItemTagsProvider.WEB_LINE_INTERACTABLE)
		val requireExistingNode = itemStack.isItem(ModItems.WEB_PATHFINDER)
			|| itemStack.isItem(ModItems.SPIDER_NEST_INTERFACE)
			|| !itemStack.isItem(ModItemTagsProvider.WEB_LINE_INTERACTABLE)

		return WebLineInteractionHandler.getTargetedNode(
			lines.values.toList(),
			eyePosition,
			lookEnd,
			snapToExistingNode,
			requireExistingNode
		)
	}

}