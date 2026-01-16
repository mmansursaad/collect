package com.yedc.projects

interface ProjectCreator {
    fun createNewProject(settingsJson: String, switchToTheNewProject: Boolean): ProjectConfigurationResult
}
