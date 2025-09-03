package com.jed.optima.android.utilities

import android.content.Context
import com.jed.optima.android.injection.DaggerUtils
import com.jed.optima.android.storage.StoragePathProvider
import com.jed.optima.android.storage.StoragePaths
import com.jed.optima.projects.ProjectDependencyFactory

class InstancesRepositoryProvider @JvmOverloads constructor(
    private val context: Context,
    private val storagePathFactory: ProjectDependencyFactory<StoragePaths> = StoragePathProvider()
) : ProjectDependencyFactory<com.jed.optima.forms.instances.InstancesRepository> {

    override fun create(projectId: String): com.jed.optima.forms.instances.InstancesRepository {
        val storagePaths = storagePathFactory.create(projectId)
        return com.jed.optima.android.database.instances.DatabaseInstancesRepository(
            context,
            storagePaths.metaDir,
            storagePaths.instancesDir,
            System::currentTimeMillis
        )
    }

    @Deprecated("Creating dependency without specified project is dangerous")
    fun create(): com.jed.optima.forms.instances.InstancesRepository {
        val currentProject =
            DaggerUtils.getComponent(com.jed.optima.android.application.Collect.getInstance()).currentProjectProvider()
                .requireCurrentProject()
        return create(currentProject.uuid)
    }
}
