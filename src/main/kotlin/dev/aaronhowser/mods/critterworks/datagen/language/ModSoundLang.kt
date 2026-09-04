package dev.aaronhowser.mods.critterworks.datagen.language

object ModSoundLang {
	const val SCOOCHWORM_FOOTSTEP_SUBTITLE = "subtitles.critterworks.scoochworm_footstep"
	const val SCOOCHWORM_KISS_SUBTITLE = "subtitles.critterworks.scoochworm_kiss"
	const val WEB_SNAP_SUBTITLE = "subtitles.critterworks.web_snap"

	fun add(provider: ModLanguageProvider) {
		provider.add(SCOOCHWORM_FOOTSTEP_SUBTITLE, "Scoochworm scooches")
		provider.add(SCOOCHWORM_KISS_SUBTITLE, "Scoochworm smooches")
		provider.add(WEB_SNAP_SUBTITLE, "Web snaps")
	}
}