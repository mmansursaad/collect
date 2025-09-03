package com.jed.optima.android.mainmenu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.jed.optima.analytics.Analytics
import com.jed.optima.android.analytics.AnalyticsEvents
import com.jed.optima.android.projects.ProjectsDataService
import com.jed.optima.projects.Project

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
