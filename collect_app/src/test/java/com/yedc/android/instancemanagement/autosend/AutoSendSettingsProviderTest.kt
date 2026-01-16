package com.yedc.android.instancemanagement.autosend

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.yedc.async.Scheduler
import com.yedc.async.network.NetworkStateProvider
import com.yedc.projects.Project
import com.yedc.settings.InMemSettingsProvider
import com.yedc.settings.enums.AutoSend
import com.yedc.settings.keys.ProjectKeys

@RunWith(AndroidJUnit4::class)
class AutoSendSettingsProviderTest {

    private val networkStateProvider: NetworkStateProvider = mock()
    private val settingsProvider = InMemSettingsProvider()
    private val projectId = Project.DEMO_PROJECT_NAME
    private val application = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `return false when autosend is disabled in settings and network is not available`() {
        val autoSendSettingsProvider = setupAutoSendSettingProvider(
            autoSendOption = AutoSend.OFF.getValue(application),
            networkType = null
        )

        assertFalse(autoSendSettingsProvider.isAutoSendEnabledInSettings(projectId))
    }

    @Test
    fun `return false when autosend is disabled in settings and network type is wifi`() {
        val autoSendSettingsProvider = setupAutoSendSettingProvider(
            autoSendOption = AutoSend.OFF.getValue(application),
            networkType = Scheduler.NetworkType.WIFI
        )

        assertFalse(autoSendSettingsProvider.isAutoSendEnabledInSettings(projectId))
    }

    @Test
    fun `return false when autosend is disabled in settings and network type is cellular`() {
        val autoSendSettingsProvider = setupAutoSendSettingProvider(
            autoSendOption = AutoSend.OFF.getValue(application),
            networkType = Scheduler.NetworkType.CELLULAR
        )

        assertFalse(autoSendSettingsProvider.isAutoSendEnabledInSettings(projectId))
    }

    @Test
    fun `return false when autosend is enabled for 'wifi_only' and network is not available`() {
        val autoSendSettingsProvider = setupAutoSendSettingProvider(
            autoSendOption = AutoSend.WIFI_ONLY.getValue(application),
            networkType = null
        )

        assertFalse(autoSendSettingsProvider.isAutoSendEnabledInSettings(projectId))
    }

    @Test
    fun `return false when autosend is enabled for 'wifi_only' and network type is cellular`() {
        val autoSendSettingsProvider = setupAutoSendSettingProvider(
            autoSendOption = AutoSend.WIFI_ONLY.getValue(application),
            networkType = Scheduler.NetworkType.CELLULAR
        )

        assertFalse(autoSendSettingsProvider.isAutoSendEnabledInSettings(projectId))
    }

    @Test
    fun `return true when autosend is enabled for 'wifi_only' and network type is wifi`() {
        val autoSendSettingsProvider = setupAutoSendSettingProvider(
            autoSendOption = AutoSend.WIFI_ONLY.getValue(application),
            networkType = Scheduler.NetworkType.WIFI
        )

        assertTrue(autoSendSettingsProvider.isAutoSendEnabledInSettings(projectId))
    }

    @Test
    fun `return false when autosend is enabled for 'cellular_only' and network is not available`() {
        val autoSendSettingsProvider = setupAutoSendSettingProvider(
            autoSendOption = AutoSend.CELLULAR_ONLY.getValue(application),
            networkType = null
        )

        assertFalse(autoSendSettingsProvider.isAutoSendEnabledInSettings(projectId))
    }

    @Test
    fun `return false when autosend is enabled for 'cellular_only' and network type is wifi`() {
        val autoSendSettingsProvider = setupAutoSendSettingProvider(
            autoSendOption = AutoSend.CELLULAR_ONLY.getValue(application),
            networkType = Scheduler.NetworkType.WIFI
        )

        assertFalse(autoSendSettingsProvider.isAutoSendEnabledInSettings(projectId))
    }

    @Test
    fun `return true when autosend is enabled for 'cellular_only' and network type is cellular`() {
        val autoSendSettingsProvider = setupAutoSendSettingProvider(
            autoSendOption = AutoSend.CELLULAR_ONLY.getValue(application),
            networkType = Scheduler.NetworkType.CELLULAR
        )

        assertTrue(autoSendSettingsProvider.isAutoSendEnabledInSettings(projectId))
    }

    @Test
    fun `return false when autosend is enabled for 'wifi_and_cellular' and network is not available`() {
        val autoSendSettingsProvider = setupAutoSendSettingProvider(
            autoSendOption = AutoSend.WIFI_AND_CELLULAR.getValue(application),
            networkType = null
        )

        assertFalse(autoSendSettingsProvider.isAutoSendEnabledInSettings(projectId))
    }

    @Test
    fun `return true when autosend is enabled for 'wifi_and_cellular' and network type is wifi`() {
        val autoSendSettingsProvider = setupAutoSendSettingProvider(
            autoSendOption = AutoSend.WIFI_AND_CELLULAR.getValue(application),
            networkType = Scheduler.NetworkType.WIFI
        )

        assertTrue(autoSendSettingsProvider.isAutoSendEnabledInSettings(projectId))
    }

    @Test
    fun `return true when autosend is enabled for 'wifi_and_cellular' and network type is cellular`() {
        val autoSendSettingsProvider = setupAutoSendSettingProvider(
            autoSendOption = AutoSend.WIFI_AND_CELLULAR.getValue(application),
            networkType = Scheduler.NetworkType.CELLULAR
        )

        assertTrue(autoSendSettingsProvider.isAutoSendEnabledInSettings(projectId))
    }

    private fun setupAutoSendSettingProvider(
        autoSendOption: String? = null,
        networkType: Scheduler.NetworkType? = null
    ): AutoSendSettingsProvider {
        whenever(networkStateProvider.currentNetwork).thenReturn(networkType)
        settingsProvider.getUnprotectedSettings(projectId).save(ProjectKeys.KEY_AUTOSEND, autoSendOption)

        return AutoSendSettingsProvider(application, networkStateProvider, settingsProvider)
    }
}
