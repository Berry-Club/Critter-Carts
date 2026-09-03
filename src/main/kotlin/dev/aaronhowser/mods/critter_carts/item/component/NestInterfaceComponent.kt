package dev.aaronhowser.mods.critter_carts.item.component

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.aaronhowser.mods.aaron.serialization.AaronExtraStreamCodecs
import io.netty.buffer.ByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.util.StringRepresentable
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemContainerContents

data class NestInterfaceComponent(
	val filterContents: ItemContainerContents,
	val color: DyeColor,
	val transferDirection: TransferDirection,
	val priority: Int
) {

	constructor() : this(ItemContainerContents.EMPTY, DyeColor.WHITE, TransferDirection.OUTPUT, 0)

	fun getFilter(): ItemStack {
		if (filterContents.slots == 0) return ItemStack.EMPTY
		return filterContents.getStackInSlot(0)
	}

	fun withFilter(filter: ItemStack): NestInterfaceComponent {
		return copy(filterContents = ItemContainerContents.fromItems(listOf(filter)))
	}

	fun withColor(newColor: DyeColor): NestInterfaceComponent = copy(color = newColor)

	fun withTransferDirection(newDirection: TransferDirection): NestInterfaceComponent {
		return copy(transferDirection = newDirection)
	}

	fun withPriority(newPriority: Int): NestInterfaceComponent = copy(priority = newPriority)

	companion object {
		val CODEC: Codec<NestInterfaceComponent> = RecordCodecBuilder.create { instance ->
			instance.group(
				ItemContainerContents.CODEC
					.optionalFieldOf("filter", ItemContainerContents.EMPTY)
					.forGetter(NestInterfaceComponent::filterContents),
				DyeColor.CODEC
					.optionalFieldOf("color", DyeColor.WHITE)
					.forGetter(NestInterfaceComponent::color),
				TransferDirection.CODEC
					.optionalFieldOf("transfer_direction", TransferDirection.OUTPUT)
					.forGetter(NestInterfaceComponent::transferDirection),
				Codec.INT
					.optionalFieldOf("priority", 0)
					.forGetter(NestInterfaceComponent::priority)
			).apply(instance, ::NestInterfaceComponent)
		}

		val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, NestInterfaceComponent> =
			StreamCodec.composite(
				ItemContainerContents.STREAM_CODEC, NestInterfaceComponent::filterContents,
				AaronExtraStreamCodecs.enumStreamCodec(DyeColor::class.java), NestInterfaceComponent::color,
				TransferDirection.STREAM_CODEC, NestInterfaceComponent::transferDirection,
				ByteBufCodecs.VAR_INT, NestInterfaceComponent::priority,
				::NestInterfaceComponent
			)
	}

	enum class TransferDirection(private val id: String) : StringRepresentable {
		INPUT("input"),
		OUTPUT("output");

		override fun getSerializedName(): String = id

		fun next(): TransferDirection = if (this == INPUT) OUTPUT else INPUT

		companion object {
			val CODEC: StringRepresentable.EnumCodec<TransferDirection> =
				StringRepresentable.fromEnum { entries.toTypedArray() }
			val STREAM_CODEC: StreamCodec<ByteBuf, TransferDirection> =
				AaronExtraStreamCodecs.enumStreamCodec(TransferDirection::class.java)
		}
	}
}