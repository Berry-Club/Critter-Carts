package dev.aaronhowser.mods.critter_carts.handler.web

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.aaronhowser.mods.aaron.serialization.AaronExtraStreamCodecs
import io.netty.buffer.ByteBuf
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.UUIDUtil
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.phys.Vec3
import java.util.UUID

sealed interface WebNode {
	val position: Vec3

	fun isLoaded(level: ServerLevel): Boolean

	fun isValid(level: ServerLevel, lines: Map<UUID, WebLine>, checkedLines: MutableSet<UUID>): Boolean

	data class BlockAnchor(
		val blockPos: BlockPos,
		val face: Direction,
		override val position: Vec3
	) : WebNode {
		override fun isLoaded(level: ServerLevel): Boolean {
			return isChunkLoaded(level, ChunkPos(blockPos))
		}

		override fun isValid(
			level: ServerLevel,
			lines: Map<UUID, WebLine>,
			checkedLines: MutableSet<UUID>
		): Boolean {
			return level.getBlockState(blockPos).isFaceSturdy(level, blockPos, face)
		}

		companion object {
			const val TYPE = "block"
			const val TYPE_ID = 0

			val CODEC: MapCodec<BlockAnchor> =
				RecordCodecBuilder.mapCodec { instance ->
					instance.group(
						BlockPos.CODEC
							.fieldOf("block_pos")
							.forGetter(BlockAnchor::blockPos),
						Direction.CODEC
							.fieldOf("face")
							.forGetter(BlockAnchor::face),
						Vec3.CODEC
							.fieldOf("position")
							.forGetter(BlockAnchor::position)
					).apply(instance, ::BlockAnchor)
				}

			val STREAM_CODEC: StreamCodec<ByteBuf, BlockAnchor> = StreamCodec.composite(
				BlockPos.STREAM_CODEC, BlockAnchor::blockPos,
				Direction.STREAM_CODEC, BlockAnchor::face,
				AaronExtraStreamCodecs.VEC3, BlockAnchor::position,
				::BlockAnchor
			)
		}
	}

	data class LineAnchor(
		val lineUuid: UUID,
		override val position: Vec3
	) : WebNode {
		override fun isLoaded(level: ServerLevel): Boolean {
			val chunkPos = ChunkPos(BlockPos.containing(position))
			return isChunkLoaded(level, chunkPos)
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

	companion object {
		private fun isChunkLoaded(level: ServerLevel, chunkPos: ChunkPos): Boolean {
			return level.hasChunk(chunkPos.x, chunkPos.z)
		}

		val CODEC: Codec<WebNode> =
			Codec.STRING.dispatch(
				"type",
				{ node ->
					when (node) {
						is BlockAnchor -> BlockAnchor.TYPE
						is LineAnchor -> LineAnchor.TYPE
					}
				},
				{ type ->
					when (type) {
						BlockAnchor.TYPE -> BlockAnchor.CODEC
						LineAnchor.TYPE -> LineAnchor.CODEC
						else -> error("Unknown web node type: $type")
					}
				}
			)

		val STREAM_CODEC: StreamCodec<ByteBuf, WebNode> =
			ByteBufCodecs.VAR_INT.dispatch(
				{ node ->
					when (node) {
						is BlockAnchor -> BlockAnchor.TYPE_ID
						is LineAnchor -> LineAnchor.TYPE_ID
					}
				},
				{ typeId ->
					when (typeId) {
						BlockAnchor.TYPE_ID -> BlockAnchor.STREAM_CODEC
						LineAnchor.TYPE_ID -> LineAnchor.STREAM_CODEC
						else -> error("Unknown web node type: $typeId")
					}
				}
			)
	}

}