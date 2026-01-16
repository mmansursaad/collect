package com.yedc.android.application.initialization

import com.yedc.analytics.Analytics
import com.yedc.settings.SettingsProvider
import com.yedc.settings.keys.ProjectKeys

class AnalyticsInitializer(
    private val analytics: Analytics,
    private val versionInformation: _root_ide_package_.com.yedc.android.version.VersionInformation,
    private val settingsProvider: SettingsProvider
) {

    fun initialize() {
        if (versionInformation.isBeta) {
            analytics.setAnalyticsCollectionEnabled(true)
        } else {
            val analyticsEnabled = settingsProvider.getUnprotectedSettings().getBoolean(ProjectKeys.KEY_ANALYTICS)
            analytics.setAnalyticsCollectionEnabled(analyticsEnabled)
        }

        Analytics.setInstance(analytics)
    }
}
