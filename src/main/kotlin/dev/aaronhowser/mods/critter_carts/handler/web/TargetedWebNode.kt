package dev.aaronhowser.mods.critter_carts.handler.web

import dev.aaronhowser.mods.critter_carts.handler.web.node.WebNode
import java.util.*

data class TargetedWebNode(
	val lineUuid: UUID?,
	val node: WebNode
)