package com.jed.optima.android.application.initialization

import android.content.Context
import com.jed.optima.analytics.Analytics
import com.jed.optima.android.preferences.Defaults
import com.jed.optima.projects.Project
import com.jed.optima.projects.ProjectsRepository
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.settings.keys.ProjectKeys

class UserPropertiesInitializer(
    private val analytics: Analytics,
    private val projectsRepository: ProjectsRepository,
    private val settingsProvider: SettingsProvider,
    private val context: Context
) {

    fun initialize() {
        val projects = projectsRepository.getAll()

        analytics.setUserProperty("ProjectsCount", projects.size.toString())

        analytics.setUserProperty(
            "UsingLegacyFormUpdate",
            projects.any { isNotUsingMatchExactly(it, context) }.toString()
        )

        analytics.setUserProperty(
            "UsingNonDefaultTheme",
            projects.any { isNotUsingDefaultTheme(it) }.toString()
        )
    }

    private fun isNotUsingMatchExactly(project: Project.Saved, context: Context): Boolean {
        val settings = settingsProvider.getUnprotectedSettings(project.uuid)
        val serverUrl = settings.getString(ProjectKeys.KEY_SERVER_URL)
        val formUpdateMode = settings.getString(ProjectKeys.KEY_FORM_UPDATE_MODE)

        val notUsingDefaultServer = serverUrl != Defaults.unprotected[ProjectKeys.KEY_SERVER_URL]
        val notUsingMatchExactly = formUpdateMode != com.jed.optima.settings.enums.FormUpdateMode.MATCH_EXACTLY.getValue(context)

        return notUsingDefaultServer && notUsingMatchExactly
    }

    private fun isNotUsingDefaultTheme(project: Project.Saved): Boolean {
        val settings = settingsProvider.getUnprotectedSettings(project.uuid)
        val theme = settings.getString(ProjectKeys.KEY_APP_THEME)
        return theme != Defaults.unprotected[ProjectKeys.KEY_APP_THEME]
    }
}
