package dev.aaronhowser.mods.critter_carts.handler.web

import dev.aaronhowser.mods.critter_carts.handler.web.line.WebLine
import java.util.UUID

class WebNetwork(
	val uuid: UUID = UUID.randomUUID()
) {

	val lines: Set<WebLine>
		field = mutableSetOf()

	internal fun addLine(line: WebLine) {
		if (lines.add(line)) {
			line.network = this
		}
	}

	internal fun addLines(lines: Collection<WebLine>) {
		for (line in lines) {
			addLine(line)
		}
	}

	internal fun removeLine(line: WebLine) {
		if (lines.remove(line) && line.network === this) {
			line.network = null
		}
	}

	internal fun clear() {
		val removedLines = lines.toList()
		for (line in removedLines) {
			removeLine(line)
		}
	}
}