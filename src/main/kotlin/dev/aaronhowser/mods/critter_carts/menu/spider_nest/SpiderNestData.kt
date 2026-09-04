package dev.aaronhowser.mods.critter_carts.menu.spider_nest

import dev.aaronhowser.mods.critter_carts.block_entity.HoppingSpiderNestBlockEntity
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.inventory.ContainerData
import kotlin.math.roundToInt

class SpiderNestData(
	private val nest: HoppingSpiderNestBlockEntity
) : ContainerData {

	override fun get(index: Int): Int {
		val spiderIndex = index / VALUES_PER_SPIDER
		val valueOffset = index % VALUES_PER_SPIDER
		val spider = nest.hoppingSpiders.getOrNull(spiderIndex) ?: return 0

		return when (valueOffset) {
			HAS_POSITION_OFFSET -> if (spider.position == null) 0 else 1
			POSITION_X_OFFSET -> encodeCoordinate(spider.position?.x)
			POSITION_Y_OFFSET -> encodeCoordinate(spider.position?.y)
			POSITION_Z_OFFSET -> encodeCoordinate(spider.position?.z)
			PHASE_OFFSET -> spider.job?.phase?.ordinal?.plus(1) ?: 0
			TRANSFER_AMOUNT_OFFSET -> spider.job?.transferAmount ?: 0
			ITEM_ID_OFFSET -> BuiltInRegistries.ITEM.getId(spider.carriedStack.item)
			ITEM_COUNT_OFFSET -> spider.carriedStack.count
			else -> 0
		}
	}

	private fun encodeCoordinate(coordinate: Double?): Int {
		return ((coordinate ?: 0.0) * COORDINATE_SCALE).roundToInt()
	}

	override fun set(index: Int, value: Int) {}

	override fun getCount(): Int = DATA_COUNT

	companion object {
		const val HAS_POSITION_OFFSET = 0
		const val POSITION_X_OFFSET = 1
		const val POSITION_Y_OFFSET = 2
		const val POSITION_Z_OFFSET = 3
		const val PHASE_OFFSET = 4
		const val TRANSFER_AMOUNT_OFFSET = 5
		const val ITEM_ID_OFFSET = 6
		const val ITEM_COUNT_OFFSET = 7
		private const val VALUES_PER_SPIDER = 8
		private const val COORDINATE_SCALE = 10.0
		const val DATA_COUNT = SpiderNestMenu.SPIDER_COUNT * VALUES_PER_SPIDER

		fun getIndex(spiderIndex: Int, valueOffset: Int): Int {
			return spiderIndex * VALUES_PER_SPIDER + valueOffset
		}
	}
}