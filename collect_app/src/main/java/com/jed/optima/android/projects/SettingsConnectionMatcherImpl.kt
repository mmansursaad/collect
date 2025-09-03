package com.jed.optima.android.projects

import org.json.JSONException
import org.json.JSONObject
import com.jed.optima.android.preferences.Defaults
import com.jed.optima.projects.ProjectsRepository
import com.jed.optima.projects.SettingsConnectionMatcher
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.settings.keys.AppConfigurationKeys
import com.jed.optima.settings.keys.ProjectKeys

class SettingsConnectionMatcherImpl(
    private val projectsRepository: ProjectsRepository,
    private val settingsProvider: SettingsProvider
) : SettingsConnectionMatcher {

    override fun getProjectWithMatchingConnection(settingsJson: String): String? {
        try {
            val jsonObject = JSONObject(settingsJson)
            val jsonSettings = jsonObject.getJSONObject(AppConfigurationKeys.GENERAL)

            val jsonUrl = try { jsonSettings.get(ProjectKeys.KEY_SERVER_URL) } catch (e: JSONException) { Defaults.unprotected[ProjectKeys.KEY_SERVER_URL]!! }
            val jsonUsername = try { jsonSettings.get(ProjectKeys.KEY_USERNAME) } catch (e: JSONException) { "" }

            projectsRepository.getAll().forEach {
                val projectSettings = settingsProvider.getUnprotectedSettings(it.uuid)
                val projectUrl = projectSettings.getString(ProjectKeys.KEY_SERVER_URL)
                val projectUsername = projectSettings.getString(ProjectKeys.KEY_USERNAME)

                if (jsonUrl.equals(projectUrl) && jsonUsername.equals(projectUsername)) {
                    return it.uuid
                }
            }
        } catch (e: JSONException) {
            return null
        }
        return null
    }
}
