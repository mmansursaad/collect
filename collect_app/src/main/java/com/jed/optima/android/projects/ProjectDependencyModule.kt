package com.jed.optima.android.projects

import com.jed.optima.android.storage.StoragePaths
import com.jed.optima.android.utilities.ChangeLocks
import com.jed.optima.entities.server.EntitySource
import com.jed.optima.entities.storage.EntitiesRepository
import com.jed.optima.forms.savepoints.SavepointsRepository
import com.jed.optima.projects.ProjectDependencyFactory
import com.jed.optima.shared.settings.Settings

/**
 * Provides all the basic/building block dependencies needed when performing logic inside a
 * project.
 */
data class ProjectDependencyModule(
    val projectId: String,
    private val settingsFactory: ProjectDependencyFactory<Settings>,
    private val formsRepositoryFactory: ProjectDependencyFactory<com.jed.optima.forms.FormsRepository>,
    private val instancesRepositoryProvider: ProjectDependencyFactory<com.jed.optima.forms.instances.InstancesRepository>,
    private val storagePathsFactory: ProjectDependencyFactory<StoragePaths>,
    private val changeLockFactory: ProjectDependencyFactory<ChangeLocks>,
    private val formSourceFactory: ProjectDependencyFactory<com.jed.optima.forms.FormSource>,
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
