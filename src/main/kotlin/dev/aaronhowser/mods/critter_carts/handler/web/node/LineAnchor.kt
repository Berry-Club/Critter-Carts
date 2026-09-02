package dev.aaronhowser.mods.critter_carts.handler.web.node

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.aaronhowser.mods.aaron.serialization.AaronExtraStreamCodecs
import dev.aaronhowser.mods.critter_carts.handler.web.WebLine
import dev.aaronhowser.mods.critter_carts.handler.web.WebLineInvalidation
import dev.aaronhowser.mods.critter_carts.handler.web.WebLineInvalidationReason
import io.netty.buffer.ByteBuf
import net.minecraft.core.BlockPos
import net.minecraft.core.UUIDUtil
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.phys.Vec3
import java.util.UUID

data class LineAnchor(
	override val uuid: UUID,
	val lineUuid: UUID,
	override val position: Vec3
) : WebNode {

	override fun isLoaded(level: ServerLevel): Boolean {
		val chunkPos = ChunkPos(BlockPos.containing(position))
		return WebNode.isChunkLoaded(level, chunkPos)
	}

	override fun getInvalidation(
		level: ServerLevel,
		lines: Map<UUID, WebLine>,
		checkedLines: MutableSet<UUID>
	): WebLineInvalidation? {
		val line = lines[lineUuid]
			?: return WebLineInvalidation(
				WebLineInvalidationReason.MISSING_LINE,
				dependencyDepth = 1
			)
		if (!line.isLoaded(level)) return null

		val invalidation = line.getInvalidation(level, lines, checkedLines) ?: return null
		return invalidation.copy(dependencyDepth = invalidation.dependencyDepth + 1)
	}

	companion object {
		const val TYPE = "line"
		const val TYPE_ID = 1

		val CODEC: MapCodec<LineAnchor> =
			RecordCodecBuilder.mapCodec { instance ->
				instance.group(
					UUIDUtil.CODEC
						.fieldOf("uuid")
						.forGetter(LineAnchor::uuid),
					UUIDUtil.CODEC
						.fieldOf("line_uuid")
						.forGetter(LineAnchor::lineUuid),
					Vec3.CODEC
						.fieldOf("position")
						.forGetter(LineAnchor::position)
				).apply(instance, ::LineAnchor)
			}

		val STREAM_CODEC: StreamCodec<ByteBuf, LineAnchor> = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, LineAnchor::uuid,
			UUIDUtil.STREAM_CODEC, LineAnchor::lineUuid,
			AaronExtraStreamCodecs.VEC3, LineAnchor::position,
			::LineAnchor
		)
	}
}