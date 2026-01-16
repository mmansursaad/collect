package com.yedc.android.utilities

import android.content.Context
import com.yedc.android.database.savepoints.DatabaseSavepointsRepository
import com.yedc.android.injection.DaggerUtils
import com.yedc.android.storage.StoragePaths
import com.yedc.forms.savepoints.SavepointsRepository
import com.yedc.projects.ProjectDependencyFactory

class SavepointsRepositoryProvider(
    private val context: Context,
    private val storagePathFactory: ProjectDependencyFactory<StoragePaths>
) : ProjectDependencyFactory<SavepointsRepository> {

    override fun create(projectId: String): SavepointsRepository {
        val storagePaths = storagePathFactory.create(projectId)
        return DatabaseSavepointsRepository(
            context,
            storagePaths.metaDir,
            storagePaths.cacheDir,
            storagePaths.instancesDir
        )
    }

    @Deprecated("Creating dependency without specified project is dangerous")
    fun create(): SavepointsRepository {
        val currentProject =
            DaggerUtils.getComponent(_root_ide_package_.com.yedc.android.application.Collect.getInstance()).currentProjectProvider()
                .requireCurrentProject()
        return create(currentProject.uuid)
    }
}
