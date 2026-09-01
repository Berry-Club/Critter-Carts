package dev.aaronhowser.mods.critter_carts.handler.web

import java.util.UUID

object ClientWebLines {
	private val lines: MutableMap<UUID, WebLine> = mutableMapOf()

	fun getLines(): Collection<WebLine> {
		return lines.values
	}

	fun addLines(newLines: Collection<WebLine>) {
		for (line in newLines) {
			lines[line.uuid] = line
		}
	}

	fun removeLine(uuid: UUID) {
		lines.remove(uuid)
	}

	fun clear() {
		lines.clear()
	}
}