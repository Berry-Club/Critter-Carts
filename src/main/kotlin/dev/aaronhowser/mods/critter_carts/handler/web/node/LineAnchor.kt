package dev.aaronhowser.mods.critter_carts.handler.web.node

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.aaronhowser.mods.aaron.serialization.AaronExtraStreamCodecs
import dev.aaronhowser.mods.critter_carts.handler.web.WebLine
import io.netty.buffer.ByteBuf
import net.minecraft.core.BlockPos
import net.minecraft.core.UUIDUtil
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.phys.Vec3
import java.util.UUID

data class LineAnchor(
	val lineUuid: UUID,
	override val position: Vec3
) : WebNode {

	override fun isLoaded(level: ServerLevel): Boolean {
		val chunkPos = ChunkPos(BlockPos.containing(position))
		return WebNode.isChunkLoaded(level, chunkPos)
	}

	override fun isValid(
		level: ServerLevel,
		lines: Map<UUID, WebLine>,
		checkedLines: MutableSet<UUID>
	): Boolean {
		val line = lines[lineUuid] ?: return false
		if (!line.isLoaded(level)) return true

		return line.isValid(level, lines, checkedLines)
	}

	companion object {
		const val TYPE = "line"
		const val TYPE_ID = 1

		val CODEC: MapCodec<LineAnchor> =
			RecordCodecBuilder.mapCodec { instance ->
				instance.group(
					UUIDUtil.CODEC
						.fieldOf("line_uuid")
						.forGetter(LineAnchor::lineUuid),
					Vec3.CODEC
						.fieldOf("position")
						.forGetter(LineAnchor::position)
				).apply(instance, ::LineAnchor)
			}

		val STREAM_CODEC: StreamCodec<ByteBuf, LineAnchor> = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, LineAnchor::lineUuid,
			AaronExtraStreamCodecs.VEC3, LineAnchor::position,
			::LineAnchor
		)
	}
}