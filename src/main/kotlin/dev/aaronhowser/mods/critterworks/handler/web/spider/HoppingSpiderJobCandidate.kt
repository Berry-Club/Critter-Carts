package dev.aaronhowser.mods.critterworks.handler.web.spider

class HoppingSpiderJobCandidate(
	val job: HoppingSpiderJob,
	val inputPriority: Int,
	val outputPriority: Int,
	val stackSize: Int
) {

	fun isPreferredOver(other: HoppingSpiderJobCandidate?): Boolean {
		if (other == null) return true

		if (inputPriority != other.inputPriority) return inputPriority > other.inputPriority

		if (outputPriority != other.outputPriority) return outputPriority > other.outputPriority

		return stackSize > other.stackSize
	}
}