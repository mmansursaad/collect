package com.jed.optima.projects

interface ProjectCreator {
    fun createNewProject(settingsJson: String, switchToTheNewProject: Boolean): ProjectConfigurationResult
}
