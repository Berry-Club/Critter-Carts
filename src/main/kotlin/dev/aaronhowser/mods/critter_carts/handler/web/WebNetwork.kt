package dev.aaronhowser.mods.critter_carts.handler.web

import java.util.UUID

class WebNetwork(
	val uuid: UUID = UUID.randomUUID()
) {

	private val _lines: MutableSet<WebLine> = mutableSetOf()

	val lines: Set<WebLine>
		get() = _lines

	internal fun addLine(line: WebLine) {
		_lines.add(line)
	}

	internal fun addLines(lines: Collection<WebLine>) {
		_lines.addAll(lines)
	}

	internal fun removeLine(line: WebLine) {
		_lines.remove(line)
	}

	internal fun clear() {
		_lines.clear()
	}
}