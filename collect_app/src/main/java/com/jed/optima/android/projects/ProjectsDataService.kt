package com.jed.optima.android.projects

import kotlinx.coroutines.flow.StateFlow
import com.jed.optima.android.application.initialization.AnalyticsInitializer
import com.jed.optima.android.application.initialization.MapsInitializer
import com.jed.optima.android.state.DataKeys
import com.jed.optima.androidshared.data.AppState
import com.jed.optima.androidshared.data.DataService
import com.jed.optima.projects.Project
import com.jed.optima.projects.ProjectsRepository
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.settings.keys.MetaKeys

class ProjectsDataService(
    appState: AppState,
    private val settingsProvider: SettingsProvider,
    private val projectsRepository: ProjectsRepository,
    private val analyticsInitializer: AnalyticsInitializer,
    private val mapsInitializer: MapsInitializer
) : DataService(appState) {

    private val currentProject by data(DataKeys.PROJECT, null) {
        val currentProjectId = getCurrentProjectId()

        if (currentProjectId != null) {
            projectsRepository.get(currentProjectId)
        } else {
            val projects = projectsRepository.getAll()

            if (projects.isNotEmpty()) {
                projects[0]
            } else {
                null
            }
        }
    }

    fun getCurrentProject(): StateFlow<Project.Saved?> {
        return currentProject.flow()
    }

    @Deprecated(
        "Most components should be passed project ID/project as a value",
        replaceWith = ReplaceWith("getCurrentProject().value!!")
    )
    fun requireCurrentProject(): Project.Saved {
        update()
        val project = getCurrentProject().value

        if (project != null) {
            return project
        } else {
            throw IllegalStateException("No current project!")
        }
    }

    fun setCurrentProject(projectId: String) {
        settingsProvider.getMetaSettings().save(MetaKeys.CURRENT_PROJECT_ID, projectId)

        analyticsInitializer.initialize()
        mapsInitializer.initialize()

        update()
    }

    private fun getCurrentProjectId(): String? {
        return settingsProvider.getMetaSettings().getString(MetaKeys.CURRENT_PROJECT_ID)
    }
}
