package dev.aaronhowser.mods.critter_carts.handler.web

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.aaronhowser.mods.critter_carts.handler.web.node.WebNode
import io.netty.buffer.ByteBuf
import net.minecraft.core.BlockPos
import net.minecraft.core.UUIDUtil
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.shapes.CollisionContext
import java.util.UUID

data class WebLine(
	val uuid: UUID,
	val firstNode: WebNode,
	val secondNode: WebNode
) {
	val length: Double
		get() = firstNode.position.distanceTo(secondNode.position)
	val intersectedChunkPositions: Set<ChunkPos> = calculateIntersectedChunkPositions()

	fun isLoaded(level: ServerLevel): Boolean {
		return firstNode.isLoaded(level) && secondNode.isLoaded(level)
	}

	fun isValid(level: ServerLevel, lines: Map<UUID, WebLine>): Boolean {
		return isValid(level, lines, mutableSetOf())
	}

	fun isValid(
		level: ServerLevel,
		lines: Map<UUID, WebLine>,
		checkedLines: MutableSet<UUID>
	): Boolean {
		if (!checkedLines.add(uuid)) return false
		if (!firstNode.isValid(level, lines, checkedLines)) return false
		if (!secondNode.isValid(level, lines, checkedLines)) return false

		checkedLines.remove(uuid)

		val clipContext = ClipContext(
			firstNode.position,
			secondNode.position,
			ClipContext.Block.COLLIDER,
			ClipContext.Fluid.NONE,
			CollisionContext.empty()
		)

		return level.clip(clipContext).type == HitResult.Type.MISS
	}

	fun getEndpointChunkPositions(): Set<ChunkPos> {
		return setOf(
			ChunkPos(BlockPos.containing(firstNode.position)),
			ChunkPos(BlockPos.containing(secondNode.position))
		)
	}

	private fun calculateIntersectedChunkPositions(): Set<ChunkPos> {
		val firstChunk = ChunkPos(BlockPos.containing(firstNode.position))
		val secondChunk = ChunkPos(BlockPos.containing(secondNode.position))
		val minChunkX = minOf(firstChunk.x, secondChunk.x)
		val maxChunkX = maxOf(firstChunk.x, secondChunk.x)
		val minChunkZ = minOf(firstChunk.z, secondChunk.z)
		val maxChunkZ = maxOf(firstChunk.z, secondChunk.z)
		val minY = minOf(firstNode.position.y, secondNode.position.y) - 1.0
		val maxY = maxOf(firstNode.position.y, secondNode.position.y) + 1.0
		val chunkPositions: MutableSet<ChunkPos> = mutableSetOf()

		for (chunkX in minChunkX..maxChunkX) {
			for (chunkZ in minChunkZ..maxChunkZ) {
				val chunkPos = ChunkPos(chunkX, chunkZ)
				val bounds = AABB(
					chunkPos.minBlockX.toDouble(),
					minY,
					chunkPos.minBlockZ.toDouble(),
					(chunkPos.maxBlockX + 1).toDouble(),
					maxY,
					(chunkPos.maxBlockZ + 1).toDouble()
				)

				if (bounds.contains(firstNode.position)
					|| bounds.clip(firstNode.position, secondNode.position).isPresent
				) {
					chunkPositions.add(chunkPos)
				}
			}
		}

		return chunkPositions
	}

	companion object {
		val CODEC: Codec<WebLine> = RecordCodecBuilder.create { instance ->
			instance.group(
				UUIDUtil.CODEC
					.fieldOf("uuid")
					.forGetter(WebLine::uuid),
				WebNode.CODEC
					.fieldOf("first_node")
					.forGetter(WebLine::firstNode),
				WebNode.CODEC
					.fieldOf("second_node")
					.forGetter(WebLine::secondNode)
			).apply(instance, ::WebLine)
		}

		val STREAM_CODEC: StreamCodec<ByteBuf, WebLine> = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, WebLine::uuid,
			WebNode.STREAM_CODEC, WebLine::firstNode,
			WebNode.STREAM_CODEC, WebLine::secondNode,
			::WebLine
		)
	}
}