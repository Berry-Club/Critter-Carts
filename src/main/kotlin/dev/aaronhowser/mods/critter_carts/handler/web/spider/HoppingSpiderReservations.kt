package dev.aaronhowser.mods.critter_carts.handler.web.spider

import java.util.*

class HoppingSpiderReservations {

	private val sources: MutableSet<Pair<UUID, Int>> = mutableSetOf()
	private val destinations: MutableSet<UUID> = mutableSetOf()

	fun reserve(job: HoppingSpiderJob) {
		sources.add(job.sourceNodeUuid to job.sourceSlot)

		destinations.add(job.destinationNodeUuid)
	}

	fun isSourceReserved(nodeUuid: UUID, slot: Int): Boolean {
		return nodeUuid to slot in sources
	}

	fun isDestinationReserved(nodeUuid: UUID): Boolean {
		return nodeUuid in destinations
	}
}