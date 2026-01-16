package com.yedc.android.configure.qr

import org.json.JSONObject
import com.yedc.android.preferences.Defaults
import com.yedc.android.projects.ProjectsDataService
import com.yedc.settings.SettingsProvider
import com.yedc.settings.keys.AppConfigurationKeys
import com.yedc.settings.keys.ProjectKeys
import com.yedc.settings.keys.ProtectedProjectKeys

class AppConfigurationGenerator(
    private val settingsProvider: SettingsProvider,
    private val projectsDataService: ProjectsDataService
) {

    fun getAppConfigurationAsJsonWithServerDetails(
        url: String,
        username: String,
        password: String
    ): String {
        val generalSettings = JSONObject().apply {
            put(ProjectKeys.KEY_SERVER_URL, url)
            put(ProjectKeys.KEY_USERNAME, username)
            put(ProjectKeys.KEY_PASSWORD, password)
        }

        return JSONObject().apply {
            put(AppConfigurationKeys.GENERAL, generalSettings)
            put(AppConfigurationKeys.ADMIN, JSONObject())
            put(AppConfigurationKeys.PROJECT, JSONObject())
        }.toString()
    }

    fun getAppConfigurationAsJson(includedPasswordKeys: Collection<String> = emptyList()): String {
        return JSONObject().apply {
            put(AppConfigurationKeys.GENERAL, getUnprotectedPrefsAsJson(includedPasswordKeys))
            put(AppConfigurationKeys.ADMIN, getProtectedPrefsAsJson(includedPasswordKeys))
            put(AppConfigurationKeys.PROJECT, getProjectDetailsAsJson())
        }.toString()
    }

    private fun getUnprotectedPrefsAsJson(includedPasswordKeys: Collection<String>): JSONObject {
        val generalPrefs = JSONObject()

        val generalSettings = settingsProvider.getUnprotectedSettings().getAll()
        val defaultGeneralSettings: Map<String, *> = Defaults.unprotected

        for (key in defaultGeneralSettings.keys) {
            if (key == ProjectKeys.KEY_PASSWORD && !includedPasswordKeys.contains(ProjectKeys.KEY_PASSWORD)) {
                continue
            }
            val value = generalSettings[key]
            if (value != null && value != defaultGeneralSettings[key]) {
                generalPrefs.put(key, value)
            }
        }
        return generalPrefs
    }

    private fun getProtectedPrefsAsJson(includedPasswordKeys: Collection<String>): JSONObject {
        val adminPrefs = JSONObject()

        val adminSettings = settingsProvider.getProtectedSettings().getAll()
        val defaultAdminSettings = Defaults.protected

        for (key in ProtectedProjectKeys.allKeys()) {
            if (
                key == ProtectedProjectKeys.KEY_ADMIN_PW &&
                !includedPasswordKeys.contains(ProtectedProjectKeys.KEY_ADMIN_PW)
            ) {
                continue
            }

            val value = adminSettings[key]
            if (value != null && value != defaultAdminSettings[key]) {
                adminPrefs.put(key, value)
            }
        }

        return adminPrefs
    }

    private fun getProjectDetailsAsJson(): JSONObject {
        val currentProject = projectsDataService.requireCurrentProject()

        return JSONObject().apply {
            put(AppConfigurationKeys.PROJECT_NAME, currentProject.name)
            put(AppConfigurationKeys.PROJECT_ICON, currentProject.icon)
            put(AppConfigurationKeys.PROJECT_COLOR, currentProject.color)
        }
    }
}
