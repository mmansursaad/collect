package com.yedc.projects

import dagger.Component
import dagger.Module
import dagger.Provides
import com.yedc.shared.strings.UUIDGenerator
import javax.inject.Singleton

interface ProjectsDependencyComponentProvider {
    val projectsDependencyComponent: ProjectsDependencyComponent
}

@Component(modules = [ProjectsDependencyModule::class])
@Singleton
interface ProjectsDependencyComponent

@Module
open class ProjectsDependencyModule {

    @Provides
    open fun providesProjectsRepository(): ProjectsRepository {
        return InMemProjectsRepository(UUIDGenerator())
    }
}
