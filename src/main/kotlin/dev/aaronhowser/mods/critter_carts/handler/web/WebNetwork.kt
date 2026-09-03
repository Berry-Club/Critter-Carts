package dev.aaronhowser.mods.critter_carts.handler.web

import java.util.UUID

class WebNetwork(
	val uuid: UUID = UUID.randomUUID()
) {

	private val mutableLines: MutableSet<WebLine> = mutableSetOf()

	val lines: Set<WebLine>
		get() = mutableLines

	internal fun addLine(line: WebLine) {
		mutableLines.add(line)
	}

	internal fun addLines(lines: Collection<WebLine>) {
		mutableLines.addAll(lines)
	}

	internal fun removeLine(line: WebLine) {
		mutableLines.remove(line)
	}

	internal fun clear() {
		mutableLines.clear()
	}
}