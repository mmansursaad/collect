package com.yedc.settings

import com.yedc.shared.settings.Settings

interface SettingsProvider {

    fun getMetaSettings(): Settings

    fun getUnprotectedSettings(projectId: String?): Settings

    fun getUnprotectedSettings(): Settings = getUnprotectedSettings(null)

    fun getProtectedSettings(projectId: String?): Settings

    fun getProtectedSettings(): Settings = getProtectedSettings(null)

    fun clearAll(projectIds: List<String>)
}
