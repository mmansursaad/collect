package com.jed.optima.android.preferences.source

import android.content.Context
import com.jed.optima.android.preferences.Defaults
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.settings.keys.MetaKeys
import com.jed.optima.shared.settings.Settings
import javax.inject.Singleton

@Singleton
class SharedPreferencesSettingsProvider(private val context: Context) : SettingsProvider {

    private val settings = mutableMapOf<String, Settings>()

    override fun getMetaSettings() = settings.getOrPut(META_SETTINGS_NAME) {
        SharedPreferencesSettings(getSharedPrefs(META_SETTINGS_NAME))
    }

    override fun getUnprotectedSettings(projectId: String?): Settings {
        val settingsId = getSettingsId(GENERAL_SETTINGS_NAME, projectId)

        return settings.getOrPut(settingsId) {
            SharedPreferencesSettings(getSharedPrefs(settingsId), Defaults.unprotected)
        }
    }

    override fun getProtectedSettings(projectId: String?): Settings {
        val settingsId = getSettingsId(ADMIN_SETTINGS_NAME, projectId)

        return settings.getOrPut(settingsId) {
            SharedPreferencesSettings(getSharedPrefs(settingsId), Defaults.protected)
        }
    }

    override fun clearAll(projectIds: List<String>) {
        projectIds.forEach {
            getUnprotectedSettings(it).clear()
            getProtectedSettings(it).clear()
        }

        getMetaSettings().clear()
        settings.clear()
    }

    private fun getSharedPrefs(name: String) =
        context.getSharedPreferences(name, Context.MODE_PRIVATE)

    private fun getSettingsId(settingName: String, projectId: String?): String {
        return if (projectId == null) {
            settingName + (getMetaSettings().getString(MetaKeys.CURRENT_PROJECT_ID))
        } else {
            settingName + projectId
        }
    }

    companion object {
        private const val META_SETTINGS_NAME = "meta"
        private const val GENERAL_SETTINGS_NAME = "general_prefs"
        private const val ADMIN_SETTINGS_NAME = "admin_prefs"
    }
}
