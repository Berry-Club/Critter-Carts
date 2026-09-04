package dev.aaronhowser.mods.critterworks.handler.web

import dev.aaronhowser.mods.critterworks.handler.web.node.WebNode
import java.util.*

data class TargetedWebNode(
	val lineUuid: UUID?,
	val node: WebNode
)