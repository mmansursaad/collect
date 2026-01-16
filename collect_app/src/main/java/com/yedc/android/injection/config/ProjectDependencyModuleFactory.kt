package com.yedc.android.injection.config

import com.yedc.android.entities.EntitiesRepositoryProvider
import com.yedc.android.formmanagement.OpenRosaClientProvider
import com.yedc.android.projects.ProjectDependencyModule
import com.yedc.android.storage.StoragePathProvider
import com.yedc.android.utilities.ChangeLockProvider
import com.yedc.android.utilities.FormsRepositoryProvider
import com.yedc.android.utilities.InstancesRepositoryProvider
import com.yedc.android.utilities.SavepointsRepositoryProvider
import com.yedc.projects.ProjectDependencyFactory
import com.yedc.settings.SettingsProvider
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
