package com.jed.optima.android.application.initialization

import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.only
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.jed.optima.analytics.Analytics
import com.jed.optima.settings.InMemSettingsProvider
import com.jed.optima.settings.keys.ProjectKeys

class AnalyticsInitializerTest {

    private val analytics = mock<Analytics>()
    private val settingsProvider = InMemSettingsProvider()
    private val versionInformation = mock<com.jed.optima.android.version.VersionInformation> {
        on { isBeta } doReturn false
    }

    @Test
    fun whenBetaVersion_enablesAnalytics() {
        whenever(versionInformation.isBeta).thenReturn(true)

        val analyticsInitializer = AnalyticsInitializer(
            analytics,
            versionInformation,
            settingsProvider
        )
        analyticsInitializer.initialize()
        verify(analytics, only()).setAnalyticsCollectionEnabled(true)
    }

    @Test
    fun whenBetaVersion_andAnalyticsDisabledInSettings_enablesAnalytics() {
        whenever(versionInformation.isBeta).thenReturn(true)
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_ANALYTICS, false)

        val analyticsInitializer = AnalyticsInitializer(
            analytics,
            versionInformation,
            settingsProvider
        )
        analyticsInitializer.initialize()
        verify(analytics, only()).setAnalyticsCollectionEnabled(true)
    }

    @Test
    fun whenAnalyticsDisabledInSettings_disablesAnalytics() {
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_ANALYTICS, false)

        val analyticsInitializer =
            AnalyticsInitializer(analytics, versionInformation, settingsProvider)
        analyticsInitializer.initialize()
        verify(analytics, only()).setAnalyticsCollectionEnabled(false)
    }

    @Test
    fun whenAnalyticsEnabledInSettings_enablesAnalytics() {
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_ANALYTICS, true)

        val analyticsInitializer =
            AnalyticsInitializer(analytics, versionInformation, settingsProvider)
        analyticsInitializer.initialize()
        verify(analytics, only()).setAnalyticsCollectionEnabled(true)
    }
}
