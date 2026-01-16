package com.yedc.android.projects

import com.yedc.projects.Project
import com.yedc.projects.ProjectConfigurationResult
import com.yedc.projects.ProjectCreator
import com.yedc.projects.ProjectsRepository
import com.yedc.settings.ODKAppSettingsImporter
import com.yedc.settings.SettingsProvider

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
