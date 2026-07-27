package dev.aaronhowser.mods.critter_carts.entity

enum class ScoochwormPartAttachment(
	val serializedName: String
) {
	NONE("none"),
	CHEST("chest"),
	SADDLE("saddle");

	companion object {
		fun fromSerializedName(serializedName: String): ScoochwormPartAttachment {
			return entries.firstOrNull {
				it.serializedName == serializedName
			} ?: NONE
		}

		fun fromNetworkId(networkId: Int): ScoochwormPartAttachment {
			return entries.getOrElse(networkId) { NONE }
		}
	}
}