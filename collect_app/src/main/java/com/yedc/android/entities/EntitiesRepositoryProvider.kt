package com.yedc.android.entities

import android.content.Context
import com.yedc.android.database.entities.DatabaseEntitiesRepository
import com.yedc.android.storage.StoragePaths
import com.yedc.entities.storage.EntitiesRepository
import com.yedc.projects.ProjectDependencyFactory

class EntitiesRepositoryProvider(
    private val context: Context,
    private val storagePathFactory: ProjectDependencyFactory<StoragePaths>
) :
    ProjectDependencyFactory<EntitiesRepository> {

    override fun create(projectId: String): EntitiesRepository {
        return DatabaseEntitiesRepository(
            context,
            storagePathFactory.create(projectId).metaDir,
            System::currentTimeMillis
        )
    }
}
