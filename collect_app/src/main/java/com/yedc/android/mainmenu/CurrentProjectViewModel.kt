package com.yedc.android.mainmenu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.yedc.analytics.Analytics
import com.yedc.android.analytics.AnalyticsEvents
import com.yedc.android.projects.ProjectsDataService
import com.yedc.projects.Project

class CurrentProjectViewModel(
    private val projectsDataService: ProjectsDataService
) : ViewModel() {

    init {
        projectsDataService.update()
    }

    val currentProject = projectsDataService.getCurrentProject().asLiveData()

    fun setCurrentProject(project: Project.Saved) {
        Analytics.log(AnalyticsEvents.SWITCH_PROJECT)
        projectsDataService.setCurrentProject(project.uuid)
    }

    fun hasCurrentProject(): Boolean {
        return projectsDataService.getCurrentProject().value != null
    }
}
