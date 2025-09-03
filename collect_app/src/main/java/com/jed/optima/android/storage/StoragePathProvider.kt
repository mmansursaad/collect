package com.jed.optima.android.storage

import com.jed.optima.android.injection.DaggerUtils
import com.jed.optima.android.projects.ProjectsDataService
import com.jed.optima.projects.ProjectDependencyFactory
import com.jed.optima.projects.ProjectsRepository
import com.jed.optima.shared.PathUtils
import timber.log.Timber
import java.io.File

class StoragePathProvider(
    private val projectsDataService: ProjectsDataService = DaggerUtils.getComponent(com.jed.optima.android.application.Collect.getInstance()).currentProjectProvider(),
    private val projectsRepository: ProjectsRepository = DaggerUtils.getComponent(com.jed.optima.android.application.Collect.getInstance()).projectsRepository(),
    val odkRootDirPath: String = com.jed.optima.android.application.Collect.getInstance().getExternalFilesDir(null)!!.absolutePath
) : ProjectDependencyFactory<StoragePaths> {

    @JvmOverloads
    @Deprecated(message = "Use create() instead")
    fun getProjectRootDirPath(projectId: String? = null): String {
        val uuid = projectId ?: projectsDataService.requireCurrentProject().uuid
        val path = getOdkDirPath(com.jed.optima.android.storage.StorageSubdirectory.PROJECTS) + File.separator + uuid

        if (!File(path).exists()) {
            File(path).mkdirs()

            try {
                val sanitizedProjectName = PathUtils.getPathSafeFileName(projectsRepository.get(uuid)!!.name)
                File(path + File.separator + sanitizedProjectName).createNewFile()
            } catch (e: Exception) {
                Timber.e(
                    Error(
                        com.jed.optima.android.utilities.FileUtils.getFilenameError(
                            projectsRepository.get(uuid)!!.name
                        )
                    )
                )
            }
        }

        return path
    }

    @JvmOverloads
    @Deprecated(message = "Use create() instead")
    fun getOdkDirPath(subdirectory: com.jed.optima.android.storage.StorageSubdirectory, projectId: String? = null): String {
        val path = when (subdirectory) {
            com.jed.optima.android.storage.StorageSubdirectory.PROJECTS,
            com.jed.optima.android.storage.StorageSubdirectory.SHARED_LAYERS -> odkRootDirPath + File.separator + subdirectory.directoryName
            com.jed.optima.android.storage.StorageSubdirectory.FORMS,
            com.jed.optima.android.storage.StorageSubdirectory.INSTANCES,
            com.jed.optima.android.storage.StorageSubdirectory.CACHE,
            com.jed.optima.android.storage.StorageSubdirectory.METADATA,
            com.jed.optima.android.storage.StorageSubdirectory.LAYERS,
            com.jed.optima.android.storage.StorageSubdirectory.SETTINGS -> getProjectRootDirPath(projectId) + File.separator + subdirectory.directoryName
        }

        if (!File(path).exists()) {
            File(path).mkdirs()
        }

        return path
    }

    @Deprecated(
        message = "Should use specific temp file or create a new file in StorageSubdirectory.CACHE instead",
        ReplaceWith(
            "getOdkDirPath(StorageSubdirectory.CACHE) + File.separator + \"tmp.jpg\""
        )
    )
    fun getTmpImageFilePath(): String {
        return getOdkDirPath(com.jed.optima.android.storage.StorageSubdirectory.CACHE) + File.separator + "tmp.jpg"
    }

    @Deprecated(
        message = "Should use specific temp file or create a new file in StorageSubdirectory.CACHE instead",
        ReplaceWith(
            "getOdkDirPath(StorageSubdirectory.CACHE) + File.separator + \"tmp.mp4\""
        )
    )
    fun getTmpVideoFilePath(): String {
        return getOdkDirPath(com.jed.optima.android.storage.StorageSubdirectory.CACHE) + File.separator + "tmp.mp4"
    }

    override fun create(projectId: String): StoragePaths {
        return StoragePaths(getProjectRootDirPath(projectId),
            getOdkDirPath(com.jed.optima.android.storage.StorageSubdirectory.FORMS, projectId),
            getOdkDirPath(com.jed.optima.android.storage.StorageSubdirectory.INSTANCES, projectId),
            getOdkDirPath(com.jed.optima.android.storage.StorageSubdirectory.CACHE, projectId),
            getOdkDirPath(com.jed.optima.android.storage.StorageSubdirectory.METADATA, projectId),
            getOdkDirPath(com.jed.optima.android.storage.StorageSubdirectory.SETTINGS, projectId),
            getOdkDirPath(com.jed.optima.android.storage.StorageSubdirectory.LAYERS, projectId)
        )
    }
}
