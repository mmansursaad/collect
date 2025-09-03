package com.jed.optima.settings

import com.jed.optima.shared.settings.InMemSettings
import com.jed.optima.shared.settings.Settings

class InMemSettingsProvider : SettingsProvider {

    private val metaSettings = InMemSettings()
    private val settings = mutableMapOf<String?, InMemSettings>()

    override fun getMetaSettings(): Settings {
        return metaSettings
    }

    override fun getUnprotectedSettings(projectId: String?): Settings {
        return settings.getOrPut("general:$projectId") { InMemSettings() }
    }

    override fun getProtectedSettings(projectId: String?): Settings {
        return settings.getOrPut("admin:$projectId") { InMemSettings() }
    }

    override fun clearAll(projectIds: List<String>) {
        settings.values.forEach { it.clear() }
        settings.clear()
        metaSettings.clear()
    }
}
