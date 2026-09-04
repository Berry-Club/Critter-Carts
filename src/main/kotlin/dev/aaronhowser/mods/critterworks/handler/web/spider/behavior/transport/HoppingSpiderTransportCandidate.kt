package dev.aaronhowser.mods.critterworks.handler.web.spider.behavior.transport

import dev.aaronhowser.mods.critterworks.handler.web.spider.HoppingSpider

class HoppingSpiderTransportCandidate(
	val spider: HoppingSpider,
	val behavior: HoppingSpiderTransportBehavior,
	val inputPriority: Int,
	val outputPriority: Int,
	val distanceToSource: Double,
	val stackSize: Int
) {

	fun isPreferredOver(other: HoppingSpiderTransportCandidate?): Boolean {
		if (other == null) return true

		if (inputPriority != other.inputPriority) return inputPriority > other.inputPriority

		if (outputPriority != other.outputPriority) return outputPriority > other.outputPriority

		val isIdle = spider.transportBehavior == null
		val otherIsIdle = other.spider.transportBehavior == null
		if (isIdle != otherIsIdle) return isIdle

		if (distanceToSource != other.distanceToSource) return distanceToSource < other.distanceToSource

		return stackSize > other.stackSize
	}
}