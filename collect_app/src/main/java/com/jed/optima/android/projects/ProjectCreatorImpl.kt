package com.jed.optima.android.projects

import com.jed.optima.projects.Project
import com.jed.optima.projects.ProjectConfigurationResult
import com.jed.optima.projects.ProjectCreator
import com.jed.optima.projects.ProjectsRepository
import com.jed.optima.settings.ODKAppSettingsImporter
import com.jed.optima.settings.SettingsProvider

class ProjectCreatorImpl(
    private val projectsRepository: ProjectsRepository,
    private val projectsDataService: ProjectsDataService,
    private val settingsImporter: ODKAppSettingsImporter,
    private val settingsProvider: SettingsProvider
) : ProjectCreator {

    override fun createNewProject(settingsJson: String, switchToTheNewProject: Boolean): ProjectConfigurationResult {
        val savedProject = projectsRepository.save(Project.New("", "", ""))
        val settingsImportingResult = settingsImporter.fromJSON(settingsJson, savedProject)

        return if (settingsImportingResult == ProjectConfigurationResult.SUCCESS) {
            if (switchToTheNewProject) {
                projectsDataService.setCurrentProject(savedProject.uuid)
            }
            settingsImportingResult
        } else {
            settingsProvider.getUnprotectedSettings(savedProject.uuid).clear()
            settingsProvider.getProtectedSettings(savedProject.uuid).clear()
            projectsRepository.delete(savedProject.uuid)
            settingsImportingResult
        }
    }
}
