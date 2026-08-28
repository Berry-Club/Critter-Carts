package dev.aaronhowser.mods.critter_carts.datagen.language

object ModAdvancementLang {

	const val ROOT_TITLE = "advancements.critter_carts.root.title"
	const val ROOT_DESC = "advancements.critter_carts.root.desc"
	const val BREAK_APPLE_SLICE_TITLE = "advancements.critter_carts.break_apple_slice.title"
	const val BREAK_APPLE_SLICE_DESC = "advancements.critter_carts.break_apple_slice.desc"
	const val INTERACT_WITH_SCOOCHWORM_TITLE = "advancements.critter_carts.interact_with_scoochworm.title"
	const val INTERACT_WITH_SCOOCHWORM_DESC = "advancements.critter_carts.interact_with_scoochworm.desc"
	const val ATTACH_TO_SCOOCHWORM_TITLE = "advancements.critter_carts.attach_to_scoochworm.title"
	const val ATTACH_TO_SCOOCHWORM_DESC = "advancements.critter_carts.attach_to_scoochworm.desc"
	const val SPLIT_SCOOCHWORM_TITLE = "advancements.critter_carts.split_scoochworm.title"
	const val SPLIT_SCOOCHWORM_DESC = "advancements.critter_carts.split_scoochworm.desc"
	const val WITNESS_HEAD_ON_COLLISION_TITLE = "advancements.critter_carts.witness_head_on_collision.title"
	const val WITNESS_HEAD_ON_COLLISION_DESC = "advancements.critter_carts.witness_head_on_collision.desc"
	const val DYE_SCOOCHWORM_TITLE = "advancements.critter_carts.dye_scoochworm.title"
	const val DYE_SCOOCHWORM_DESC = "advancements.critter_carts.dye_scoochworm.desc"
	const val EAT_DYEBERRY_TITLE = "advancements.critter_carts.eat_dyeberry.title"
	const val EAT_DYEBERRY_DESC = "advancements.critter_carts.eat_dyeberry.desc"
	const val EAT_AARONBERRY_TITLE = "advancements.critter_carts.eat_aaronberry.title"
	const val EAT_AARONBERRY_DESC = "advancements.critter_carts.eat_aaronberry.desc"

	fun add(provider: ModLanguageProvider) {
		provider.apply {
			add(ROOT_TITLE, "Critter Carts")
			add(ROOT_DESC, "Critter Carts")
			add(BREAK_APPLE_SLICE_TITLE, "The Big Apple")
			add(BREAK_APPLE_SLICE_DESC, "Find and break into a Big Apple, which spawns in Lush Caves")
			add(INTERACT_WITH_SCOOCHWORM_TITLE, "The Hungry Hungry Scoochworm")
			add(INTERACT_WITH_SCOOCHWORM_DESC, "Interact with a Scoochworm")
			add(ATTACH_TO_SCOOCHWORM_TITLE, "Dress to Impress")
			add(ATTACH_TO_SCOOCHWORM_DESC, "Attach something to a Scoochworm")
			add(SPLIT_SCOOCHWORM_TITLE, "How Babies Are Made")
			add(SPLIT_SCOOCHWORM_DESC, "Split a Scoochworm with Shears")
			add(WITNESS_HEAD_ON_COLLISION_TITLE, "Smoochworm")
			add(WITNESS_HEAD_ON_COLLISION_DESC, "Get two Scoochworms to run into each other head-on")
			add(DYE_SCOOCHWORM_TITLE, "Dye a Scoochworm")
			add(DYE_SCOOCHWORM_DESC, "Dye a Scoochworm with a dyeberry")
			add(EAT_DYEBERRY_TITLE, "Eat a Dyeberry")
			add(EAT_DYEBERRY_DESC, "Eat a dyeberry")
			add(EAT_AARONBERRY_TITLE, "You're Eating My Flesh!")
			add(EAT_AARONBERRY_DESC, "Eat an Aaronberry")
		}
	}
}