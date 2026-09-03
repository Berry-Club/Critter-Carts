package dev.aaronhowser.mods.critter_carts.handler.web

import dev.aaronhowser.mods.critter_carts.handler.web.line.WebLine
import java.util.UUID

class WebNetwork(
	val uuid: UUID = UUID.randomUUID()
) {

	private val mutableLines: MutableSet<WebLine> = mutableSetOf()

	val lines: Set<WebLine>
		get() = mutableLines

	internal fun addLine(line: WebLine) {
		if (mutableLines.add(line)) {
			line.network = this
		}
	}

	internal fun addLines(lines: Collection<WebLine>) {
		for (line in lines) {
			addLine(line)
		}
	}

	internal fun removeLine(line: WebLine) {
		if (mutableLines.remove(line) && line.network === this) {
			line.network = null
		}
	}

	internal fun clear() {
		val removedLines = mutableLines.toList()
		for (line in removedLines) {
			removeLine(line)
		}
	}
}