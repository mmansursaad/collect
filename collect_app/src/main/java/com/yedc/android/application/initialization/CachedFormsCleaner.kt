package com.yedc.android.application.initialization

import com.yedc.android.projects.ProjectDependencyModule
import com.yedc.projects.ProjectDependencyFactory
import com.yedc.projects.ProjectsRepository
import com.yedc.upgrade.Upgrade
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
