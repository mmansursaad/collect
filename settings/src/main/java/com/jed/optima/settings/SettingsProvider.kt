package com.jed.optima.settings

import com.jed.optima.shared.settings.Settings

interface SettingsProvider {

    fun getMetaSettings(): Settings

    fun getUnprotectedSettings(projectId: String?): Settings

    fun getUnprotectedSettings(): Settings = getUnprotectedSettings(null)

    fun getProtectedSettings(projectId: String?): Settings

    fun getProtectedSettings(): Settings = getProtectedSettings(null)

    fun clearAll(projectIds: List<String>)
}
