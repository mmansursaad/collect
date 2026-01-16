package com.yedc.android.storage

import com.yedc.android.injection.DaggerUtils
import com.yedc.android.projects.ProjectsDataService
import com.yedc.projects.ProjectDependencyFactory
import com.yedc.projects.ProjectsRepository
import com.yedc.shared.PathUtils
import timber.log.Timber
import java.io.File

class StoragePathProvider(
    private val projectsDataService: ProjectsDataService = DaggerUtils.getComponent(
        _root_ide_package_.com.yedc.android.application.Collect.getInstance()).currentProjectProvider(),
    private val projectsRepository: ProjectsRepository = DaggerUtils.getComponent(_root_ide_package_.com.yedc.android.application.Collect.getInstance()).projectsRepository(),
    val odkRootDirPath: String = _root_ide_package_.com.yedc.android.application.Collect.getInstance().getExternalFilesDir(null)!!.absolutePath
) : ProjectDependencyFactory<StoragePaths> {

    @JvmOverloads
    @Deprecated(message = "Use create() instead")
    fun getProjectRootDirPath(projectId: String? = null): String {
        val uuid = projectId ?: projectsDataService.requireCurrentProject().uuid
        val path = getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.PROJECTS) + File.separator + uuid

        if (!File(path).exists()) {
            File(path).mkdirs()

            try {
                val sanitizedProjectName = PathUtils.getPathSafeFileName(projectsRepository.get(uuid)!!.name)
                File(path + File.separator + sanitizedProjectName).createNewFile()
            } catch (e: Exception) {
                Timber.e(
                    Error(
                        _root_ide_package_.com.yedc.android.utilities.FileUtils.getFilenameError(
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
    fun getOdkDirPath(subdirectory: _root_ide_package_.com.yedc.android.storage.StorageSubdirectory, projectId: String? = null): String {
        val path = when (subdirectory) {
            _root_ide_package_.com.yedc.android.storage.StorageSubdirectory.PROJECTS,
            _root_ide_package_.com.yedc.android.storage.StorageSubdirectory.SHARED_LAYERS -> odkRootDirPath + File.separator + subdirectory.directoryName
            _root_ide_package_.com.yedc.android.storage.StorageSubdirectory.FORMS,
            _root_ide_package_.com.yedc.android.storage.StorageSubdirectory.INSTANCES,
            _root_ide_package_.com.yedc.android.storage.StorageSubdirectory.CACHE,
            _root_ide_package_.com.yedc.android.storage.StorageSubdirectory.METADATA,
            _root_ide_package_.com.yedc.android.storage.StorageSubdirectory.LAYERS,
            _root_ide_package_.com.yedc.android.storage.StorageSubdirectory.SETTINGS -> getProjectRootDirPath(projectId) + File.separator + subdirectory.directoryName
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
        return getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.CACHE) + File.separator + "tmp.jpg"
    }

    @Deprecated(
        message = "Should use specific temp file or create a new file in StorageSubdirectory.CACHE instead",
        ReplaceWith(
            "getOdkDirPath(StorageSubdirectory.CACHE) + File.separator + \"tmp.mp4\""
        )
    )
    fun getTmpVideoFilePath(): String {
        return getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.CACHE) + File.separator + "tmp.mp4"
    }

    override fun create(projectId: String): StoragePaths {
        return StoragePaths(getProjectRootDirPath(projectId),
            getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.FORMS, projectId),
            getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.INSTANCES, projectId),
            getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.CACHE, projectId),
            getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.METADATA, projectId),
            getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.SETTINGS, projectId),
            getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.LAYERS, projectId)
        )
    }
}
