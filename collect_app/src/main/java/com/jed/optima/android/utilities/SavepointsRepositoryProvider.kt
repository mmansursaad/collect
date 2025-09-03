package com.jed.optima.android.utilities

import android.content.Context
import com.jed.optima.android.database.savepoints.DatabaseSavepointsRepository
import com.jed.optima.android.injection.DaggerUtils
import com.jed.optima.android.storage.StoragePaths
import com.jed.optima.forms.savepoints.SavepointsRepository
import com.jed.optima.projects.ProjectDependencyFactory

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
            DaggerUtils.getComponent(com.jed.optima.android.application.Collect.getInstance()).currentProjectProvider()
                .requireCurrentProject()
        return create(currentProject.uuid)
    }
}
