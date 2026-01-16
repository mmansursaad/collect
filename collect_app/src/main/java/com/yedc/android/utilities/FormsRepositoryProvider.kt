package com.yedc.android.utilities

import android.content.Context
import com.yedc.android.injection.DaggerUtils
import com.yedc.android.storage.StoragePathProvider
import com.yedc.android.storage.StoragePaths
import com.yedc.forms.savepoints.SavepointsRepository
import com.yedc.projects.ProjectDependencyFactory

class FormsRepositoryProvider @JvmOverloads constructor(
    private val context: Context,
    private val storagePathFactory: ProjectDependencyFactory<StoragePaths> = StoragePathProvider(),
    private val savepointsRepositoryProvider: ProjectDependencyFactory<SavepointsRepository> = SavepointsRepositoryProvider(
        context,
        storagePathFactory
    )
) : ProjectDependencyFactory<com.yedc.forms.FormsRepository> {

    private val clock = { System.currentTimeMillis() }

    override fun create(projectId: String): com.yedc.forms.FormsRepository {
        val storagePaths = storagePathFactory.create(projectId)
        return _root_ide_package_.com.yedc.android.database.forms.DatabaseFormsRepository(
            context,
            storagePaths.metaDir,
            storagePaths.formsDir,
            storagePaths.cacheDir,
            clock,
            savepointsRepositoryProvider.create(projectId)
        )
    }

    @Deprecated("Creating dependency without specified project is dangerous")
    fun create(): com.yedc.forms.FormsRepository {
        val currentProject =
            DaggerUtils.getComponent(_root_ide_package_.com.yedc.android.application.Collect.getInstance()).currentProjectProvider()
                .requireCurrentProject()
        return create(currentProject.uuid)
    }
}
