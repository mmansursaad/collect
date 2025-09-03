package com.jed.optima.android.utilities

import android.content.Context
import com.jed.optima.android.injection.DaggerUtils
import com.jed.optima.android.storage.StoragePathProvider
import com.jed.optima.android.storage.StoragePaths
import com.jed.optima.forms.savepoints.SavepointsRepository
import com.jed.optima.projects.ProjectDependencyFactory

class FormsRepositoryProvider @JvmOverloads constructor(
    private val context: Context,
    private val storagePathFactory: ProjectDependencyFactory<StoragePaths> = StoragePathProvider(),
    private val savepointsRepositoryProvider: ProjectDependencyFactory<SavepointsRepository> = SavepointsRepositoryProvider(
        context,
        storagePathFactory
    )
) : ProjectDependencyFactory<com.jed.optima.forms.FormsRepository> {

    private val clock = { System.currentTimeMillis() }

    override fun create(projectId: String): com.jed.optima.forms.FormsRepository {
        val storagePaths = storagePathFactory.create(projectId)
        return com.jed.optima.android.database.forms.DatabaseFormsRepository(
            context,
            storagePaths.metaDir,
            storagePaths.formsDir,
            storagePaths.cacheDir,
            clock,
            savepointsRepositoryProvider.create(projectId)
        )
    }

    @Deprecated("Creating dependency without specified project is dangerous")
    fun create(): com.jed.optima.forms.FormsRepository {
        val currentProject =
            DaggerUtils.getComponent(com.jed.optima.android.application.Collect.getInstance()).currentProjectProvider()
                .requireCurrentProject()
        return create(currentProject.uuid)
    }
}
