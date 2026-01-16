package com.yedc.android.injection.config

import com.yedc.projects.ProjectsDependencyModule
import com.yedc.projects.ProjectsRepository

class CollectProjectsDependencyModule(
    private val appDependencyComponent: _root_ide_package_.com.yedc.android.injection.config.AppDependencyComponent
) : ProjectsDependencyModule() {
    override fun providesProjectsRepository(): ProjectsRepository {
        return appDependencyComponent.projectsRepository()
    }
}
