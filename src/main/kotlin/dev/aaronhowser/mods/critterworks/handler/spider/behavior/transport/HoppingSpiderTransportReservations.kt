package dev.aaronhowser.mods.critterworks.handler.spider.behavior.transport

import java.util.*

class HoppingSpiderTransportReservations {

	private val sources: MutableSet<Pair<UUID, Int>> = mutableSetOf()
	private val destinations: MutableSet<UUID> = mutableSetOf()

	fun reserve(behavior: HoppingSpiderTransportBehavior) {
		sources.add(behavior.sourceNodeUuid to behavior.sourceSlot)

		destinations.add(behavior.destinationNodeUuid)
	}

	fun isSourceReserved(nodeUuid: UUID, slot: Int): Boolean {
		return nodeUuid to slot in sources
	}

	fun isDestinationReserved(nodeUuid: UUID): Boolean {
		return nodeUuid in destinations
	}
}