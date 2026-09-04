package dev.aaronhowser.mods.critterworks.datagen.language

object ModAdvancementLang {

	const val ROOT_TITLE = "advancements.critterworks.root.title"
	const val ROOT_DESC = "advancements.critterworks.root.desc"
	const val BREAK_APPLE_SLICE_TITLE = "advancements.critterworks.break_apple_slice.title"
	const val BREAK_APPLE_SLICE_DESC = "advancements.critterworks.break_apple_slice.desc"
	const val INTERACT_WITH_SCOOCHWORM_TITLE = "advancements.critterworks.interact_with_scoochworm.title"
	const val INTERACT_WITH_SCOOCHWORM_DESC = "advancements.critterworks.interact_with_scoochworm.desc"
	const val ATTACH_TO_SCOOCHWORM_TITLE = "advancements.critterworks.attach_to_scoochworm.title"
	const val ATTACH_TO_SCOOCHWORM_DESC = "advancements.critterworks.attach_to_scoochworm.desc"
	const val SPLIT_SCOOCHWORM_TITLE = "advancements.critterworks.split_scoochworm.title"
	const val SPLIT_SCOOCHWORM_DESC = "advancements.critterworks.split_scoochworm.desc"
	const val WITNESS_HEAD_ON_COLLISION_TITLE = "advancements.critterworks.witness_head_on_collision.title"
	const val WITNESS_HEAD_ON_COLLISION_DESC = "advancements.critterworks.witness_head_on_collision.desc"
	const val DYE_SCOOCHWORM_TITLE = "advancements.critterworks.dye_scoochworm.title"
	const val DYE_SCOOCHWORM_DESC = "advancements.critterworks.dye_scoochworm.desc"
	const val EAT_DYEBERRY_TITLE = "advancements.critterworks.eat_dyeberry.title"
	const val EAT_DYEBERRY_DESC = "advancements.critterworks.eat_dyeberry.desc"
	const val EAT_AARONBERRY_TITLE = "advancements.critterworks.eat_aaronberry.title"
	const val EAT_AARONBERRY_DESC = "advancements.critterworks.eat_aaronberry.desc"

	fun add(provider: ModLanguageProvider) {
		provider.apply {
			add(ROOT_TITLE, "Critterworks")
			add(ROOT_DESC, "Critterworks")
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