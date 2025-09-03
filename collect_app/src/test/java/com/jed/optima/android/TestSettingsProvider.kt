package com.jed.optima.android

import androidx.test.core.app.ApplicationProvider
import com.jed.optima.android.injection.DaggerUtils
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.shared.settings.Settings

// Use just for testing
object TestSettingsProvider {
    @JvmStatic
    fun getSettingsProvider(): SettingsProvider {
        return DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<com.jed.optima.android.application.Collect>()).settingsProvider()
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
