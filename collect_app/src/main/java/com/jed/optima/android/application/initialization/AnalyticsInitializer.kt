package com.jed.optima.android.application.initialization

import com.jed.optima.analytics.Analytics
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.settings.keys.ProjectKeys

class AnalyticsInitializer(
    private val analytics: Analytics,
    private val versionInformation: com.jed.optima.android.version.VersionInformation,
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
