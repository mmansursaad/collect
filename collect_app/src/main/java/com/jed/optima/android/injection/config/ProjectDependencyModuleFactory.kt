package com.jed.optima.android.injection.config

import com.jed.optima.android.entities.EntitiesRepositoryProvider
import com.jed.optima.android.formmanagement.OpenRosaClientProvider
import com.jed.optima.android.projects.ProjectDependencyModule
import com.jed.optima.android.storage.StoragePathProvider
import com.jed.optima.android.utilities.ChangeLockProvider
import com.jed.optima.android.utilities.FormsRepositoryProvider
import com.jed.optima.android.utilities.InstancesRepositoryProvider
import com.jed.optima.android.utilities.SavepointsRepositoryProvider
import com.jed.optima.projects.ProjectDependencyFactory
import com.jed.optima.settings.SettingsProvider
import javax.inject.Inject

class ProjectDependencyModuleFactory @Inject constructor(
    private val settingsProvider: SettingsProvider,
    private val formsRepositoryProvider: FormsRepositoryProvider,
    private val instancesRepositoryProvider: InstancesRepositoryProvider,
    private val storagePathProvider: StoragePathProvider,
    private val changeLockProvider: ChangeLockProvider,
    private val openRosaClientProvider: OpenRosaClientProvider,
    private val savepointsRepositoryProvider: SavepointsRepositoryProvider,
    private val entitiesRepositoryProvider: EntitiesRepositoryProvider,
) : ProjectDependencyFactory<ProjectDependencyModule> {
    override fun create(projectId: String): ProjectDependencyModule {
        return ProjectDependencyModule(
            projectId,
            settingsProvider::getUnprotectedSettings,
            formsRepositoryProvider,
            instancesRepositoryProvider,
            storagePathProvider,
            changeLockProvider,
            { openRosaClientProvider.create(projectId) },
            savepointsRepositoryProvider,
            entitiesRepositoryProvider,
            { openRosaClientProvider.create(projectId) }
        )
    }
}
