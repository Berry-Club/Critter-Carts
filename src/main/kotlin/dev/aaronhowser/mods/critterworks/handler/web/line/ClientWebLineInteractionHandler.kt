package dev.aaronhowser.mods.critterworks.handler.web.line

import dev.aaronhowser.mods.aaron.misc.AaronExtensions.isItem
import dev.aaronhowser.mods.critterworks.datagen.tag.ModItemTagsProvider
import dev.aaronhowser.mods.critterworks.handler.web.TargetedWebNode
import dev.aaronhowser.mods.critterworks.handler.web.WebLineInteractionHandler
import dev.aaronhowser.mods.critterworks.handler.web.node.WebBlockAnchor
import dev.aaronhowser.mods.critterworks.packet.client_to_server.WebLineInteractPacket
import dev.aaronhowser.mods.critterworks.registry.ModItems
import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.client.event.InputEvent

object ClientWebLineInteractionHandler {

	fun handleInteraction(event: InputEvent.InteractionKeyMappingTriggered) {
		if (!event.isUseItem) return
		interactWithTarget(event)
	}

	fun getHoveredAnchor(
		player: Player,
		eyePosition: Vec3,
		viewVector: Vec3
	): TargetedWebNode? {
		val interactionHand = getInteractionHand(player) ?: InteractionHand.MAIN_HAND
		val targetedNode = getTargetedNode(
			player,
			eyePosition,
			viewVector,
			interactionHand
		) ?: return null

		val heldStack = player.getItemInHand(interactionHand)
		if (heldStack.isItem(ModItemTagsProvider.WEB_LINE_INTERACTABLE)) {
			return targetedNode
		}

		if (!targetsInstalledInterface(targetedNode)) return null

		return targetedNode
	}

	private fun interactWithTarget(event: InputEvent.InteractionKeyMappingTriggered) {
		val player = Minecraft.getInstance().player ?: return
		val interactionHand = getInteractionHand(player) ?: event.hand
		val targetedNode = getTargetedNode(
			player,
			player.eyePosition,
			player.lookAngle,
			interactionHand
		) ?: return

		val heldStack = player.getItemInHand(interactionHand)
		if (!heldStack.isItem(ModItemTagsProvider.WEB_LINE_INTERACTABLE)
			&& !targetsInstalledInterface(targetedNode)
		) {
			return
		}

		if (event.hand == interactionHand) {
			sendInteraction(targetedNode, interactionHand)
		}

		event.isCanceled = true
	}

	private fun sendInteraction(targetedNode: TargetedWebNode, hand: InteractionHand) {
		val targetsNode = targetedNode.lineUuid == null
		val targetUuid = targetedNode.lineUuid ?: targetedNode.node.uuid

		WebLineInteractPacket(
			targetUuid,
			targetsNode,
			targetedNode.node.position,
			hand
		).messageServer()
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

	private fun getTargetedNode(
		player: Player,
		eyePosition: Vec3,
		viewVector: Vec3,
		interactionHand: InteractionHand
	): TargetedWebNode? {
		val lookEnd = eyePosition.add(viewVector.scale(player.blockInteractionRange()))

		val lines = ClientWebLines.getLines()
		val installedInterface = WebLineInteractionHandler.getTargetedNode(
			lines,
			eyePosition,
			lookEnd,
			snapToExistingNode = true,
			requireExistingNode = true
		)

		if (installedInterface != null && targetsInstalledInterface(installedInterface)) {
			if (isBehindCurrentHit(installedInterface, eyePosition)) return null

			return installedInterface
		}

		val heldStack = player.getItemInHand(interactionHand)
		return WebLineInteractionHandler.getTargetedNode(
			lines,
			eyePosition,
			lookEnd,
			shouldSnapToExistingNode(heldStack),
			requiresExistingNode(heldStack)
		)
	}

	private fun targetsInstalledInterface(targetedNode: TargetedWebNode): Boolean {
		if (targetedNode.lineUuid != null) return false

		val blockAnchor = targetedNode.node as? WebBlockAnchor ?: return false
		return blockAnchor.hasNestInterface
	}

	private fun isBehindCurrentHit(targetedNode: TargetedWebNode, eyePosition: Vec3): Boolean {
		val hitResult = Minecraft.getInstance().hitResult ?: return false
		if (hitResult.type == HitResult.Type.MISS) return false

		val hitDistanceSquared = eyePosition.distanceToSqr(hitResult.location)
		val interfaceDistanceSquared = eyePosition.distanceToSqr(targetedNode.node.position)
		return hitDistanceSquared < interfaceDistanceSquared
	}

	private fun shouldSnapToExistingNode(itemStack: ItemStack): Boolean {
		return itemStack.isItem(ModItems.WEB_FLUID)
			|| requiresExistingNode(itemStack)
	}

	private fun requiresExistingNode(itemStack: ItemStack): Boolean {
		return itemStack.isItem(ModItems.WEB_PATHFINDER)
			|| itemStack.isItem(ModItems.SPIDER_NEST_INTERFACE)
			|| !itemStack.isItem(ModItemTagsProvider.WEB_LINE_INTERACTABLE)
	}

}