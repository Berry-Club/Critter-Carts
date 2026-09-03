package dev.aaronhowser.mods.critter_carts.handler.web

import dev.aaronhowser.mods.critter_carts.handler.web.line.WebLine
import java.util.UUID

class WebNetwork(
	val uuid: UUID = UUID.randomUUID()
) {

	private val _lines: MutableSet<WebLine> = mutableSetOf()

	val lines: Set<WebLine>
		get() = _lines

	internal fun addLine(line: WebLine) {
		if (_lines.add(line)) {
			line.network = this
		}
	}

	internal fun addLines(lines: Collection<WebLine>) {
		for (line in lines) {
			addLine(line)
		}
	}

	internal fun removeLine(line: WebLine) {
		if (_lines.remove(line) && line.network === this) {
			line.network = null
		}
	}

	internal fun clear() {
		val removedLines = _lines.toList()
		for (line in removedLines) {
			removeLine(line)
		}
	}
}