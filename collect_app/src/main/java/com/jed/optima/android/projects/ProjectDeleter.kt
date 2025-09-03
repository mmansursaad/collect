package com.jed.optima.android.projects

import com.jed.optima.android.storage.StoragePathProvider
import com.jed.optima.android.utilities.ChangeLockProvider
import com.jed.optima.android.utilities.InstancesRepositoryProvider
import com.jed.optima.db.sqlite.DatabaseConnection
import com.jed.optima.projects.Project
import com.jed.optima.projects.ProjectsRepository
import com.jed.optima.settings.SettingsProvider
import java.io.File

class ProjectDeleter(
    private val projectsRepository: ProjectsRepository,
    private val projectsDataService: ProjectsDataService,
    private val formUpdateScheduler: com.jed.optima.android.backgroundwork.FormUpdateScheduler,
    private val instanceSubmitScheduler: com.jed.optima.android.backgroundwork.InstanceSubmitScheduler,
    private val instancesRepositoryProvider: InstancesRepositoryProvider,
    private val storagePathProvider: StoragePathProvider,
    private val changeLockProvider: ChangeLockProvider,
    private val settingsProvider: SettingsProvider
) {
    fun deleteProject(projectId: String = projectsDataService.requireCurrentProject().uuid): DeleteProjectResult {
        return when {
            unsentInstancesDetected(projectId) -> DeleteProjectResult.UnsentInstances
            runningBackgroundJobsDetected(projectId) -> DeleteProjectResult.RunningBackgroundJobs
            else -> performProjectDeletion(projectId)
        }
    }

    private fun unsentInstancesDetected(projectId: String): Boolean {
        return instancesRepositoryProvider.create(projectId).getAllByStatus(
            com.jed.optima.forms.instances.Instance.STATUS_INCOMPLETE,
            com.jed.optima.forms.instances.Instance.STATUS_INVALID,
            com.jed.optima.forms.instances.Instance.STATUS_VALID,
            com.jed.optima.forms.instances.Instance.STATUS_NEW_EDIT,
            com.jed.optima.forms.instances.Instance.STATUS_COMPLETE,
            com.jed.optima.forms.instances.Instance.STATUS_SUBMISSION_FAILED
        ).isNotEmpty()
    }

    private fun runningBackgroundJobsDetected(projectId: String): Boolean {
        val acquiredFormLock = changeLockProvider.getFormLock(projectId).withLock { acquiredLock ->
            acquiredLock
        }
        val acquiredInstanceLock = changeLockProvider.getInstanceLock(projectId).withLock { acquiredLock ->
            acquiredLock
        }

        return !acquiredFormLock || !acquiredInstanceLock
    }

    private fun performProjectDeletion(projectId: String): DeleteProjectResult {
        formUpdateScheduler.cancelUpdates(projectId)
        instanceSubmitScheduler.cancelSubmit(projectId)

        settingsProvider.getUnprotectedSettings(projectId).clear()
        settingsProvider.getProtectedSettings(projectId).clear()

        projectsRepository.delete(projectId)

        File(storagePathProvider.getProjectRootDirPath(projectId)).deleteRecursively()

        DatabaseConnection.cleanUp()

        return try {
            projectsDataService.requireCurrentProject()
            DeleteProjectResult.DeletedSuccessfullyInactiveProject
        } catch (e: IllegalStateException) {
            if (projectsRepository.getAll().isEmpty()) {
                DeleteProjectResult.DeletedSuccessfullyLastProject
            } else {
                val newProject = projectsRepository.getAll()[0]
                projectsDataService.setCurrentProject(newProject.uuid)
                DeleteProjectResult.DeletedSuccessfullyCurrentProject(newProject)
            }
        }
    }
}

sealed class DeleteProjectResult {
    object UnsentInstances : DeleteProjectResult()

    object RunningBackgroundJobs : DeleteProjectResult()

    object DeletedSuccessfullyLastProject : DeleteProjectResult()

    object DeletedSuccessfullyInactiveProject : DeleteProjectResult()

    data class DeletedSuccessfullyCurrentProject(val newCurrentProject: Project.Saved) : DeleteProjectResult()
}
