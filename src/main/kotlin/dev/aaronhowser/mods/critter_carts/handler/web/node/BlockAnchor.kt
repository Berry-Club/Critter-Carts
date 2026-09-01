package dev.aaronhowser.mods.critter_carts.handler.web.node

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.aaronhowser.mods.aaron.serialization.AaronExtraStreamCodecs
import dev.aaronhowser.mods.critter_carts.handler.web.WebLine
import io.netty.buffer.ByteBuf
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.phys.Vec3
import java.util.UUID

data class BlockAnchor(
	val blockPos: BlockPos,
	val face: Direction,
	override val position: Vec3
) : WebNode {

	override fun isLoaded(level: ServerLevel): Boolean {
		return WebNode.isChunkLoaded(level, ChunkPos(blockPos))
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