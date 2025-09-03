package com.jed.optima.android.application.initialization

import com.jed.optima.android.projects.ProjectDependencyModule
import com.jed.optima.projects.ProjectDependencyFactory
import com.jed.optima.projects.ProjectsRepository
import com.jed.optima.upgrade.Upgrade
import java.io.File

class CachedFormsCleaner(
    private val projectsRepository: ProjectsRepository,
    private val projectDependencyModuleFactory: ProjectDependencyFactory<ProjectDependencyModule>
) : Upgrade {
    override fun key() = null

    override fun run() {
        projectsRepository.getAll().forEach { project ->
            val projectDependencyModule = projectDependencyModuleFactory.create(project.uuid)
            File(projectDependencyModule.cacheDir)
                .listFiles { file -> file.name.endsWith(".formdef") }
                ?.forEach { file -> file.delete() }
        }
    }
}
