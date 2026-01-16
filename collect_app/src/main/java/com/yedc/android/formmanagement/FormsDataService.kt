package com.yedc.android.formmanagement

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow
import com.yedc.android.formmanagement.download.FormDownloadException
import com.yedc.android.formmanagement.metadata.FormMetadataParser
import com.yedc.android.notifications.Notifier
import com.yedc.android.projects.ProjectDependencyModule
import com.yedc.android.state.DataKeys
import com.yedc.androidshared.data.AppState
import com.yedc.androidshared.data.DataService
import com.yedc.forms.FormSourceException
import com.yedc.projects.ProjectDependencyFactory
import com.yedc.settings.keys.ProjectKeys
import java.io.File
import java.util.function.Supplier
import java.util.stream.Collectors

class FormsDataService(
    appState: AppState,
    private val notifier: Notifier,
    private val projectDependencyModuleFactory: ProjectDependencyFactory<ProjectDependencyModule>,
    private val clock: Supplier<Long>
) : DataService(appState) {

    private val forms by qualifiedData(DataKeys.FORMS, emptyList<com.yedc.forms.Form>()) { projectId ->
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        projectDependencies.formsRepository.all
    }

    private val syncing by qualifiedData(DataKeys.SYNC_STATUS_SYNCING, false)
    private val serverError by qualifiedData<FormSourceException?>(DataKeys.SYNC_STATUS_ERROR, null)
    private val diskError by qualifiedData<String?>(DataKeys.DISK_ERROR, null)

    fun getForms(projectId: String): Flow<List<com.yedc.forms.Form>> {
        return forms.flow(projectId)
    }

    fun isSyncing(projectId: String): LiveData<Boolean> {
        return syncing.flow(projectId).asLiveData()
    }

    fun getServerError(projectId: String): LiveData<FormSourceException?> {
        return serverError.flow(projectId).asLiveData()
    }

    fun getDiskError(projectId: String): LiveData<String?> {
        return diskError.flow(projectId).asLiveData()
    }

    fun clear(projectId: String) {
        serverError.set(projectId, null)
    }

    fun refresh(projectId: String) {
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        projectDependencies.formsLock.withLock { acquiredLock ->
            if (acquiredLock) {
                startSync(projectId)
                syncWithStorage(projectId)
                update(projectId)
                finishSyncWithStorage(projectId)
            }
        }
    }

    fun downloadForms(
        projectId: String,
        forms: List<ServerFormDetails>,
        progressReporter: (Int, Int) -> Unit,
        isCancelled: () -> Boolean
    ): Map<ServerFormDetails, FormDownloadException?> {
        val results = mutableMapOf<ServerFormDetails, FormDownloadException?>()

        val projectDependencyModule = projectDependencyModuleFactory.create(projectId)
        projectDependencyModule.formsLock.withLock { acquiredLock ->
            if (acquiredLock) {
                val formDownloader =
                    formDownloader(projectDependencyModule, clock)

                results.putAll(ServerFormUseCases.downloadForms(
                    forms,
                    formDownloader,
                    progressReporter,
                    isCancelled
                ))
            }
        }
        return results
    }

    /**
     * Downloads updates for the project's already downloaded forms. If Automatic download is
     * disabled the user will just be notified that there are updates available.
     */
    fun downloadUpdates(projectId: String) {
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        projectDependencies.formsLock.withLock { acquiredLock ->
            if (acquiredLock) {
                syncWithStorage(projectId)

                val serverFormsDetailsFetcher = serverFormsDetailsFetcher(projectDependencies)
                val formDownloader = formDownloader(projectDependencies, clock)

                try {
                    val serverForms: List<ServerFormDetails> =
                        serverFormsDetailsFetcher.fetchFormDetails()
                    val updatedForms =
                        serverForms.stream().filter { obj: ServerFormDetails -> obj.isUpdated }
                            .collect(Collectors.toList())
                    if (updatedForms.isNotEmpty()) {
                        if (projectDependencies.generalSettings.getBoolean(ProjectKeys.KEY_AUTOMATIC_UPDATE)) {
                            val results = ServerFormUseCases.downloadForms(
                                updatedForms,
                                formDownloader
                            )

                            notifier.onUpdatesDownloaded(results, projectId)
                        } else {
                            notifier.onUpdatesAvailable(updatedForms, projectId)
                        }
                    }

                    update(projectId)
                } catch (_: FormSourceException) {
                    // Ignored
                }
            }
        }
    }

    /**
     * Downloads new forms, updates existing forms and deletes forms that are no longer part of
     * the project's form list.
     */
    @JvmOverloads
    fun matchFormsWithServer(projectId: String, notify: Boolean = true): Boolean {
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        return projectDependencies.formsLock.withLock { acquiredLock ->
            if (acquiredLock) {
                startSync(projectId)
                syncWithStorage(projectId)

                val serverFormsDetailsFetcher = serverFormsDetailsFetcher(projectDependencies)
                val formDownloader = formDownloader(projectDependencies, clock)

                val serverFormsSynchronizer =
                    _root_ide_package_.com.yedc.android.formmanagement.matchexactly.ServerFormsSynchronizer(
                        serverFormsDetailsFetcher,
                        projectDependencies.formsRepository,
                        projectDependencies.instancesRepository,
                        formDownloader
                    )

                val exception = try {
                    serverFormsSynchronizer.synchronize()
                    if (notify) {
                        notifier.onSync(null, projectId)
                    }

                    null
                } catch (e: FormSourceException) {
                    if (notify) {
                        notifier.onSync(e, projectId)
                    }

                    e
                }

                update(projectId)
                finishSyncWithServer(projectId, exception)
                exception == null
            } else {
                false
            }
        }
    }

    fun deleteForm(projectId: String, formId: Long) {
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        LocalFormUseCases.deleteForm(
            projectDependencies.formsRepository,
            projectDependencies.instancesRepository,
            formId
        )

        update(projectId)
    }

    private fun syncWithStorage(projectId: String) {
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        val error = LocalFormUseCases.synchronizeWithDisk(
            projectDependencies.formsRepository,
            projectDependencies.formsDir
        )

        diskError.set(projectId, error)
    }

    private fun startSync(projectId: String) {
        syncing.set(projectId, true)
    }

    private fun finishSyncWithServer(projectId: String, exception: FormSourceException? = null) {
        serverError.set(projectId, exception)
        syncing.set(projectId, false)
    }

    private fun finishSyncWithStorage(projectId: String) {
        syncing.set(projectId, false)
    }
}

private fun formDownloader(
    projectDependencyModule: ProjectDependencyModule,
    clock: Supplier<Long>
): _root_ide_package_.com.yedc.android.formmanagement.download.ServerFormDownloader {
    return _root_ide_package_.com.yedc.android.formmanagement.download.ServerFormDownloader(
        projectDependencyModule.formSource,
        projectDependencyModule.formsRepository,
        File(projectDependencyModule.cacheDir),
        projectDependencyModule.formsDir,
        FormMetadataParser,
        clock,
        projectDependencyModule.entitiesRepository,
        projectDependencyModule.entitySource
    )
}

private fun serverFormsDetailsFetcher(
    projectDependencyModule: ProjectDependencyModule
): ServerFormsDetailsFetcher {
    return ServerFormsDetailsFetcher(
        projectDependencyModule.formsRepository,
        projectDependencyModule.formSource
    )
}
