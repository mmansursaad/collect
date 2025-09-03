package com.jed.optima.android.instancemanagement

import kotlinx.coroutines.flow.StateFlow
import com.jed.optima.analytics.Analytics
import com.jed.optima.android.analytics.AnalyticsEvents
import com.jed.optima.android.formentry.FormEntryUseCases
import com.jed.optima.android.formmanagement.CollectFormEntryControllerFactory
import com.jed.optima.android.instancemanagement.autosend.FormAutoSendMode
import com.jed.optima.android.instancemanagement.autosend.InstanceAutoSendFetcher
import com.jed.optima.android.instancemanagement.autosend.getAutoSendMode
import com.jed.optima.android.notifications.Notifier
import com.jed.optima.android.projects.ProjectDependencyModule
import com.jed.optima.android.state.DataKeys
import com.jed.optima.android.utilities.FormsUploadResultInterpreter
import com.jed.optima.androidshared.data.AppState
import com.jed.optima.androidshared.data.DataService
import com.jed.optima.metadata.PropertyManager
import com.jed.optima.projects.ProjectDependencyFactory
import java.io.File

class InstancesDataService(
    appState: AppState,
    private val instanceSubmitScheduler: com.jed.optima.android.backgroundwork.InstanceSubmitScheduler,
    private val projectDependencyModuleFactory: ProjectDependencyFactory<ProjectDependencyModule>,
    private val notifier: Notifier,
    private val propertyManager: PropertyManager,
    private val httpInterface: com.jed.optima.openrosa.http.OpenRosaHttpInterface,
    onUpdate: () -> Unit
) : DataService(appState, onUpdate) {

    private val editableCount by qualifiedData(DataKeys.INSTANCES_EDITABLE_COUNT, 0) { projectId ->
        val projectDependencyModule = projectDependencyModuleFactory.create(projectId)
        val instancesRepository = projectDependencyModule.instancesRepository
        instancesRepository.getCountByStatus(
            com.jed.optima.forms.instances.Instance.STATUS_INCOMPLETE,
            com.jed.optima.forms.instances.Instance.STATUS_INVALID,
            com.jed.optima.forms.instances.Instance.STATUS_VALID,
            com.jed.optima.forms.instances.Instance.STATUS_NEW_EDIT
        )
    }

    private val sendableCount by qualifiedData(DataKeys.INSTANCES_SENDABLE_COUNT, 0) { projectId ->
        val projectDependencyModule = projectDependencyModuleFactory.create(projectId)
        val instancesRepository = projectDependencyModule.instancesRepository
        instancesRepository.getCountByStatus(
            com.jed.optima.forms.instances.Instance.STATUS_COMPLETE,
            com.jed.optima.forms.instances.Instance.STATUS_SUBMISSION_FAILED
        )
    }

    private val sentCount by qualifiedData(DataKeys.INSTANCES_SENT_COUNT, 0) { projectId ->
        val projectDependencyModule = projectDependencyModuleFactory.create(projectId)
        val instancesRepository = projectDependencyModule.instancesRepository
        instancesRepository.getCountByStatus(
            com.jed.optima.forms.instances.Instance.STATUS_SUBMITTED,
            com.jed.optima.forms.instances.Instance.STATUS_SUBMISSION_FAILED
        )
    }

    private val instances by qualifiedData(DataKeys.INSTANCES, emptyList()) { projectId ->
        val projectDependencyModule = projectDependencyModuleFactory.create(projectId)
        val instancesRepository = projectDependencyModule.instancesRepository
        instancesRepository.all
    }

    fun getEditableCount(projectId: String): StateFlow<Int> = editableCount.flow(projectId)
    fun getSendableCount(projectId: String): StateFlow<Int> = sendableCount.flow(projectId)
    fun getSentCount(projectId: String): StateFlow<Int> = sentCount.flow(projectId)

    fun getInstances(projectId: String): StateFlow<List<com.jed.optima.forms.instances.Instance>> {
        return instances.flow(projectId)
    }

    fun finalizeAllDrafts(projectId: String): FinalizeAllResult {
        val projectDependencyModule = projectDependencyModuleFactory.create(projectId)
        val instancesRepository = projectDependencyModule.instancesRepository
        val formsRepository = projectDependencyModule.formsRepository
        val savepointsRepository = projectDependencyModule.savepointsRepository
        val entitiesRepository = projectDependencyModule.entitiesRepository

        val projectRootDir = File(projectDependencyModule.rootDir)

        val instances = instancesRepository.getAllByStatus(
            com.jed.optima.forms.instances.Instance.STATUS_INCOMPLETE,
            com.jed.optima.forms.instances.Instance.STATUS_INVALID,
            com.jed.optima.forms.instances.Instance.STATUS_VALID,
            com.jed.optima.forms.instances.Instance.STATUS_NEW_EDIT
        )

        val result = instances.fold(FinalizeAllResult(0, 0, false)) { result, instance ->
            val formDefAndForm = FormEntryUseCases.loadFormDef(
                instance,
                formsRepository,
                projectRootDir,
                com.jed.optima.android.utilities.ExternalizableFormDefCache()
            )

            if (formDefAndForm == null) {
                result.copy(failureCount = result.failureCount + 1)
            } else {
                val (formDef, form) = formDefAndForm

                val formMediaDir = File(form.formMediaPath)
                val formEntryController =
                    CollectFormEntryControllerFactory(
                        entitiesRepository,
                        projectDependencyModule.generalSettings
                    ).create(formDef, formMediaDir, instance)
                val formController =
                    FormEntryUseCases.loadDraft(form, instance, formEntryController)
                if (formController == null) {
                    result.copy(failureCount = result.failureCount + 1)
                } else {
                    val savePoint = savepointsRepository.get(form.dbId, instance.dbId)
                    val needsEncrypted = form.basE64RSAPublicKey != null
                    val newResult = if (savePoint != null) {
                        Analytics.log(AnalyticsEvents.BULK_FINALIZE_SAVE_POINT)
                        result.copy(
                            failureCount = result.failureCount + 1,
                            unsupportedInstances = true
                        )
                    } else if (needsEncrypted) {
                        Analytics.log(AnalyticsEvents.BULK_FINALIZE_ENCRYPTED_FORM)
                        result.copy(
                            failureCount = result.failureCount + 1,
                            unsupportedInstances = true
                        )
                    } else {
                        val finalizedInstance = FormEntryUseCases.finalizeDraft(
                            formController,
                            instancesRepository,
                            entitiesRepository
                        )

                        if (finalizedInstance == null) {
                            result.copy(failureCount = result.failureCount + 1)
                        } else {
                            instanceFinalized(projectId, form)
                            result
                        }
                    }

                    com.jed.optima.android.application.Collect.getInstance().externalDataManager?.close()
                    newResult
                }
            }
        }

        update(projectId)

        return result.copy(successCount = instances.size - result.failureCount)
    }

    fun deleteInstances(projectId: String, instanceIds: LongArray): Boolean {
        val projectDependencyModule = projectDependencyModuleFactory.create(projectId)
        val instancesRepository = projectDependencyModule.instancesRepository
        val formsRepository = projectDependencyModule.formsRepository

        return projectDependencyModule.instancesLock.withLock { acquiredLock: Boolean ->
            if (acquiredLock) {
                instanceIds.forEach { instanceId ->
                    InstanceDeleter(
                        instancesRepository,
                        formsRepository
                    ).delete(
                        instanceId
                    )
                }

                update(projectId)
                true
            } else {
                false
            }
        }
    }

    fun reset(projectId: String): Boolean {
        val projectDependencyModule =
            projectDependencyModuleFactory.create(projectId)
        val instancesRepository = projectDependencyModule.instancesRepository

        return projectDependencyModule.instancesLock.withLock { acquiredLock: Boolean ->
            if (acquiredLock) {
                val grouped = instancesRepository.all.groupBy { it.editOf ?: it.dbId }
                grouped.values.forEach { group ->
                    group.sortedByDescending { it.editNumber }
                        .forEach {
                            if (it.isDeletable()) {
                                instancesRepository.delete(it.dbId)
                            }
                        }
                }
                update(projectId)
                true
            } else {
                false
            }
        }
    }

    fun sendInstances(projectId: String, formAutoSend: Boolean = false): Boolean {
        val projectDependencyModule =
            projectDependencyModuleFactory.create(projectId)

        val instanceSubmitter = InstanceSubmitter(
            projectDependencyModule.formsRepository,
            projectDependencyModule.generalSettings,
            propertyManager,
            httpInterface,
            projectDependencyModule.instancesRepository
        )

        return projectDependencyModule.instancesLock.withLock { acquiredLock: Boolean ->
            if (acquiredLock) {
                val toUpload = InstanceAutoSendFetcher.getInstancesToAutoSend(
                    projectDependencyModule.instancesRepository,
                    projectDependencyModule.formsRepository,
                    formAutoSend
                )

                if (toUpload.isNotEmpty()) {
                    val results = instanceSubmitter.submitInstances(toUpload)
                    notifier.onSubmission(results, projectDependencyModule.projectId)
                    update(projectId)

                    FormsUploadResultInterpreter.allFormsUploadedSuccessfully(results)
                } else {
                    true
                }
            } else {
                false
            }
        }
    }

    fun instanceFinalized(projectId: String, form: com.jed.optima.forms.Form) {
        if (form.getAutoSendMode() == FormAutoSendMode.FORCED) {
            instanceSubmitScheduler.scheduleFormAutoSend(projectId)
        } else {
            instanceSubmitScheduler.scheduleAutoSend(projectId)
        }
    }

    fun editInstance(instanceFilePath: String, projectId: String): InstanceEditResult {
        val projectDependencyModule = projectDependencyModuleFactory.create(projectId)

        return LocalInstancesUseCases.editInstance(
            instanceFilePath,
            projectDependencyModule.instancesDir,
            projectDependencyModule.instancesRepository,
            projectDependencyModule.formsRepository
        )
    }
}

data class FinalizeAllResult(
    val successCount: Int,
    val failureCount: Int,
    val unsupportedInstances: Boolean
)
