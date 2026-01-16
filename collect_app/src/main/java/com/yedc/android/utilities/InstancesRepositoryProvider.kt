package com.yedc.android.utilities

import android.content.Context
import com.yedc.android.injection.DaggerUtils
import com.yedc.android.storage.StoragePathProvider
import com.yedc.android.storage.StoragePaths
import com.yedc.projects.ProjectDependencyFactory

class InstancesRepositoryProvider @JvmOverloads constructor(
    private val context: Context,
    private val storagePathFactory: ProjectDependencyFactory<StoragePaths> = StoragePathProvider()
) : ProjectDependencyFactory<com.yedc.forms.instances.InstancesRepository> {

    override fun create(projectId: String): com.yedc.forms.instances.InstancesRepository {
        val storagePaths = storagePathFactory.create(projectId)
        return _root_ide_package_.com.yedc.android.database.instances.DatabaseInstancesRepository(
            context,
            storagePaths.metaDir,
            storagePaths.instancesDir,
            System::currentTimeMillis
        )
    }

    @Deprecated("Creating dependency without specified project is dangerous")
    fun create(): com.yedc.forms.instances.InstancesRepository {
        val currentProject =
            DaggerUtils.getComponent(_root_ide_package_.com.yedc.android.application.Collect.getInstance()).currentProjectProvider()
                .requireCurrentProject()
        return create(currentProject.uuid)
    }
}
