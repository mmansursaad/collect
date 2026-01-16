package com.yedc.android.preferences

import com.yedc.android.BuildConfig
import com.yedc.shared.settings.Settings

object SettingsExt {
    fun Settings.getExperimentalOptIn(key: String): Boolean {
        val versionInformation =
            _root_ide_package_.com.yedc.android.version.VersionInformation { BuildConfig.VERSION_NAME }

        return if (!versionInformation.isRelease) {
            this.getBoolean(key)
        } else {
            false
        }
    }
}
