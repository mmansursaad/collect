package com.yedc.android.projects

import com.yedc.android.storage.StoragePaths
import com.yedc.android.utilities.ChangeLocks
import com.yedc.entities.server.EntitySource
import com.yedc.entities.storage.EntitiesRepository
import com.yedc.forms.savepoints.SavepointsRepository
import com.yedc.projects.ProjectDependencyFactory
import com.yedc.shared.settings.Settings

/**
 * Provides all the basic/building block dependencies needed when performing logic inside a
 * project.
 */
data class ProjectDependencyModule(
    val projectId: String,
    private val settingsFactory: ProjectDependencyFactory<Settings>,
    private val formsRepositoryFactory: ProjectDependencyFactory<com.yedc.forms.FormsRepository>,
    private val instancesRepositoryProvider: ProjectDependencyFactory<com.yedc.forms.instances.InstancesRepository>,
    private val storagePathsFactory: ProjectDependencyFactory<StoragePaths>,
    private val changeLockFactory: ProjectDependencyFactory<ChangeLocks>,
    private val formSourceFactory: ProjectDependencyFactory<com.yedc.forms.FormSource>,
    private val savepointsRepositoryFactory: ProjectDependencyFactory<SavepointsRepository>,
    private val entitiesRepositoryFactory: ProjectDependencyFactory<EntitiesRepository>,
    private val entitySourceFactory: ProjectDependencyFactory<EntitySource>
) {
    val generalSettings by lazy { settingsFactory.create(projectId) }
    val formsRepository by lazy { formsRepositoryFactory.create(projectId) }
    val instancesRepository by lazy { instancesRepositoryProvider.create(projectId) }
    val formSource by lazy { formSourceFactory.create(projectId) }
    val formsLock by lazy { changeLockFactory.create(projectId).formsLock }
    val instancesLock by lazy { changeLockFactory.create(projectId).instancesLock }
    val formsDir by lazy { storagePathsFactory.create(projectId).formsDir }
    val cacheDir by lazy { storagePathsFactory.create(projectId).cacheDir }
    val entitiesRepository by lazy { entitiesRepositoryFactory.create(projectId) }
    val savepointsRepository by lazy { savepointsRepositoryFactory.create(projectId) }
    val rootDir by lazy { storagePathsFactory.create(projectId).rootDir }
    val instancesDir by lazy { storagePathsFactory.create(projectId).instancesDir }
    val entitySource by lazy { entitySourceFactory.create(projectId) }
}
