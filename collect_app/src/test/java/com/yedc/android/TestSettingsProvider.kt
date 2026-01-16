package com.yedc.android

import androidx.test.core.app.ApplicationProvider
import com.yedc.android.injection.DaggerUtils
import com.yedc.settings.SettingsProvider
import com.yedc.shared.settings.Settings

// Use just for testing
object TestSettingsProvider {
    @JvmStatic
    fun getSettingsProvider(): SettingsProvider {
        return DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<_root_ide_package_.com.yedc.android.application.Collect>()).settingsProvider()
    }

    @JvmStatic
    @JvmOverloads
    fun getUnprotectedSettings(uuid: String? = null): Settings {
        return getSettingsProvider().getUnprotectedSettings(uuid)
    }

    @JvmStatic
    fun getProtectedSettings(): Settings {
        return getSettingsProvider().getProtectedSettings()
    }
}
