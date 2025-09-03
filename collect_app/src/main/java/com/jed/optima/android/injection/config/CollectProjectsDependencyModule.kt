package com.jed.optima.android.injection.config

import com.jed.optima.projects.ProjectsDependencyModule
import com.jed.optima.projects.ProjectsRepository

class CollectProjectsDependencyModule(
    private val appDependencyComponent: com.jed.optima.android.injection.config.AppDependencyComponent
) : ProjectsDependencyModule() {
    override fun providesProjectsRepository(): ProjectsRepository {
        return appDependencyComponent.projectsRepository()
    }
}
