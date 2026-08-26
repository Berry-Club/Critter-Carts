package dev.aaronhowser.mods.critter_carts.item

import dev.aaronhowser.mods.critter_carts.entity.data.WormColor
import dev.aaronhowser.mods.critter_carts.registry.ModMobEffects
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.Item

class DyeberryItem(
	val color: WormColor,
	properties: Properties
) : Item(properties) {

	constructor(color: WormColor) : this(color, getProperties(color))

	companion object {
		const val EAT_DURATION = 20 * 60
		const val POTION_DURATION = 20 * 60 * 5

		fun getProperties(wormColor: WormColor): Properties {
			val foodProperties = FoodProperties.Builder()
				.nutrition(2)
				.saturationModifier(0.1f)
				.alwaysEdible()
				.effect(
					{ MobEffectInstance(ModMobEffects.getDyedEffect(wormColor), EAT_DURATION) },
					1f
				)
				.build()

			return Properties().food(foodProperties)
		}
	}

}