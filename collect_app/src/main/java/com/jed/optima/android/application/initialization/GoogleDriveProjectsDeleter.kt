package com.jed.optima.android.application.initialization

import com.jed.optima.android.projects.DeleteProjectResult
import com.jed.optima.android.projects.ProjectDeleter
import com.jed.optima.projects.ProjectsRepository
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.settings.keys.ProjectKeys
import com.jed.optima.upgrade.Upgrade

class GoogleDriveProjectsDeleter(
    private val projectsRepository: ProjectsRepository,
    private val settingsProvider: SettingsProvider,
    private val projectDeleter: ProjectDeleter
) : Upgrade {
    override fun key() = null

    override fun run() {
        projectsRepository.getAll().forEach {
            val unprotectedSettings = settingsProvider.getUnprotectedSettings(it.uuid)
            val protocol = unprotectedSettings.getString(ProjectKeys.KEY_PROTOCOL)

            if (protocol == ProjectKeys.PROTOCOL_GOOGLE_SHEETS) {
                // try to delete
                val result = projectDeleter.deleteProject(it.uuid)

                // if project cannot be deleted then convert it to ODK protocol
                if (result == DeleteProjectResult.UnsentInstances || result == DeleteProjectResult.RunningBackgroundJobs) {
                    unprotectedSettings.save(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_SERVER)
                    unprotectedSettings.save(ProjectKeys.KEY_SERVER_URL, "https://example.com")
                    projectsRepository.save(it.copy(isOldGoogleDriveProject = true))
                }
            }
        }
    }
}
