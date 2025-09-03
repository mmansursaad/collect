package com.jed.optima.android.entities

import android.content.Context
import com.jed.optima.android.database.entities.DatabaseEntitiesRepository
import com.jed.optima.android.storage.StoragePaths
import com.jed.optima.entities.storage.EntitiesRepository
import com.jed.optima.projects.ProjectDependencyFactory

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
