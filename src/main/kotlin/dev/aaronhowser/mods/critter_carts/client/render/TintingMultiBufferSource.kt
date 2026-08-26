package dev.aaronhowser.mods.critter_carts.client.render

import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.util.FastColor

class TintingMultiBufferSource(
	private val delegate: MultiBufferSource,
	private val tintColor: Int
) : MultiBufferSource {

	override fun getBuffer(renderType: RenderType): VertexConsumer {
		return TintingVertexConsumer(
			delegate.getBuffer(renderType),
			tintColor
		)
	}

	@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
	private class TintingVertexConsumer(
		private val delegate: VertexConsumer,
		tintColor: Int
	) : VertexConsumer by delegate {

		private val tintRed = FastColor.ARGB32.red(tintColor)
		private val tintGreen = FastColor.ARGB32.green(tintColor)
		private val tintBlue = FastColor.ARGB32.blue(tintColor)

		override fun setColor(
			red: Int,
			green: Int,
			blue: Int,
			alpha: Int
		): VertexConsumer {
			return delegate.setColor(
				multiply(red, tintRed),
				multiply(green, tintGreen),
				multiply(blue, tintBlue),
				alpha
			)
		}

		private fun multiply(
			channel: Int,
			tintChannel: Int
		): Int {
			return channel * tintChannel / 255
		}
	}
}