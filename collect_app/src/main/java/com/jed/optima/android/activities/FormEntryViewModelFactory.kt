package com.jed.optima.android.activities

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import org.javarosa.core.model.actions.recordaudio.RecordAudioActions
import org.javarosa.core.model.instance.TreeReference
import com.jed.optima.android.entities.EntitiesRepositoryProvider
import com.jed.optima.android.formentry.BackgroundAudioViewModel.RecordAudioActionRegistry
import com.jed.optima.android.formentry.FormEndViewModel
import com.jed.optima.android.formentry.FormOpeningMode
import com.jed.optima.android.formentry.FormSessionRepository
import com.jed.optima.android.formentry.PrinterWidgetViewModel
import com.jed.optima.android.instancemanagement.InstancesDataService
import com.jed.optima.android.instancemanagement.autosend.AutoSendSettingsProvider
import com.jed.optima.android.projects.ProjectsDataService
import com.jed.optima.android.utilities.ChangeLockProvider
import com.jed.optima.android.utilities.FormsRepositoryProvider
import com.jed.optima.android.utilities.InstancesRepositoryProvider
import com.jed.optima.android.utilities.MediaUtils
import com.jed.optima.android.utilities.SavepointsRepositoryProvider
import com.jed.optima.async.Scheduler
import com.jed.optima.audiorecorder.recording.AudioRecorder
import com.jed.optima.permissions.PermissionsChecker
import com.jed.optima.permissions.PermissionsProvider
import com.jed.optima.printer.HtmlPrinter
import com.jed.optima.qrcode.zxing.QRCodeCreator
import com.jed.optima.settings.SettingsProvider
import java.util.function.BiConsumer

class FormEntryViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val mode: String?,
    private val sessionId: String,
    private val scheduler: Scheduler,
    private val formSessionRepository: FormSessionRepository,
    private val mediaUtils: MediaUtils,
    private val audioRecorder: AudioRecorder,
    private val projectsDataService: ProjectsDataService,
    private val entitiesRepositoryProvider: EntitiesRepositoryProvider,
    private val settingsProvider: SettingsProvider,
    private val permissionsChecker: PermissionsChecker,
    private val fusedLocationClient: com.jed.optima.location.LocationClient,
    private val permissionsProvider: PermissionsProvider,
    private val autoSendSettingsProvider: AutoSendSettingsProvider,
    private val formsRepositoryProvider: FormsRepositoryProvider,
    private val instancesRepositoryProvider: InstancesRepositoryProvider,
    private val savepointsRepositoryProvider: SavepointsRepositoryProvider,
    private val qrCodeCreator: QRCodeCreator,
    private val htmlPrinter: HtmlPrinter,
    private val instancesDataService: InstancesDataService,
    private val changeLockProvider: ChangeLockProvider
) : AbstractSavedStateViewModelFactory(owner, null) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        val projectId = projectsDataService.requireCurrentProject().uuid

        return when (modelClass) {
            com.jed.optima.android.formentry.FormEntryViewModel::class.java -> com.jed.optima.android.formentry.FormEntryViewModel(
                System::currentTimeMillis,
                scheduler,
                formSessionRepository,
                sessionId,
                formsRepositoryProvider.create(projectId),
                changeLockProvider.create(projectId)
            )

            com.jed.optima.android.formentry.saving.FormSaveViewModel::class.java -> {
                com.jed.optima.android.formentry.saving.FormSaveViewModel(
                    handle,
                    System::currentTimeMillis,
                    com.jed.optima.android.formentry.saving.DiskFormSaver(),
                    mediaUtils,
                    scheduler,
                    audioRecorder,
                    projectsDataService,
                    formSessionRepository.get(sessionId),
                    entitiesRepositoryProvider.create(projectId),
                    instancesRepositoryProvider.create(projectId),
                    savepointsRepositoryProvider.create(projectId),
                    instancesDataService
                )
            }

            com.jed.optima.android.formentry.BackgroundAudioViewModel::class.java -> {
                val recordAudioActionRegistry =
                    if (mode == FormOpeningMode.VIEW_SENT) {
                        object : RecordAudioActionRegistry {
                            override fun register(listener: BiConsumer<TreeReference, String?>) {}
                            override fun unregister() {}
                        }
                    } else {
                        object : RecordAudioActionRegistry {
                            override fun register(listener: BiConsumer<TreeReference, String?>) {
                                RecordAudioActions.setRecordAudioListener { absoluteTargetRef: TreeReference, quality: String? ->
                                    listener.accept(absoluteTargetRef, quality)
                                }
                            }

                            override fun unregister() {
                                RecordAudioActions.setRecordAudioListener(null)
                            }
                        }
                    }

                com.jed.optima.android.formentry.BackgroundAudioViewModel(
                    audioRecorder,
                    settingsProvider.getUnprotectedSettings(),
                    recordAudioActionRegistry,
                    permissionsChecker,
                    System::currentTimeMillis,
                    formSessionRepository.get(sessionId)
                )
            }

            com.jed.optima.android.formentry.backgroundlocation.BackgroundLocationViewModel::class.java -> {
                val locationManager =
                    com.jed.optima.android.formentry.backgroundlocation.BackgroundLocationManager(
                        fusedLocationClient,
                        com.jed.optima.android.formentry.backgroundlocation.BackgroundLocationHelper(
                            permissionsProvider,
                            settingsProvider.getUnprotectedSettings(),
                            formSessionRepository,
                            sessionId
                        )
                    )

                com.jed.optima.android.formentry.backgroundlocation.BackgroundLocationViewModel(
                    locationManager
                )
            }

            com.jed.optima.android.formentry.audit.IdentityPromptViewModel::class.java -> com.jed.optima.android.formentry.audit.IdentityPromptViewModel()

            FormEndViewModel::class.java -> FormEndViewModel(
                formSessionRepository,
                sessionId,
                settingsProvider,
                autoSendSettingsProvider
            )

            PrinterWidgetViewModel::class.java -> PrinterWidgetViewModel(scheduler, qrCodeCreator, htmlPrinter)

            else -> throw IllegalArgumentException()
        } as T
    }
}
