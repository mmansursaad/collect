package com.jed.optima.android.preferences

import com.jed.optima.android.BuildConfig
import com.jed.optima.android.version.VersionInformation
import com.jed.optima.shared.settings.Settings

object SettingsExt {
    fun Settings.getExperimentalOptIn(key: String): Boolean {
        val versionInformation =
            com.jed.optima.android.version.VersionInformation { BuildConfig.VERSION_NAME }

        return if (!versionInformation.isRelease) {
            this.getBoolean(key)
        } else {
            false
        }
    }
}
