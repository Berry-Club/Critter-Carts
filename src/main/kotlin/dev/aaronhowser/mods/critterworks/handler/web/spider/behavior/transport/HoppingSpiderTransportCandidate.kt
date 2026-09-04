package dev.aaronhowser.mods.critterworks.handler.web.spider.behavior.transport

class HoppingSpiderTransportCandidate(
	val behavior: HoppingSpiderTransportBehavior,
	val inputPriority: Int,
	val outputPriority: Int,
	val stackSize: Int
) {

	fun isPreferredOver(other: HoppingSpiderTransportCandidate?): Boolean {
		if (other == null) return true

		if (inputPriority != other.inputPriority) return inputPriority > other.inputPriority

		if (outputPriority != other.outputPriority) return outputPriority > other.outputPriority

		return stackSize > other.stackSize
	}
}