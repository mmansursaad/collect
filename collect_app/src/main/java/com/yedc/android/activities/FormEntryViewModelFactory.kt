package com.yedc.android.activities

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import org.javarosa.core.model.actions.recordaudio.RecordAudioActions
import org.javarosa.core.model.instance.TreeReference
import com.yedc.android.entities.EntitiesRepositoryProvider
import com.yedc.android.formentry.BackgroundAudioViewModel.RecordAudioActionRegistry
import com.yedc.android.formentry.FormEndViewModel
import com.yedc.android.formentry.FormSessionRepository
import com.yedc.android.formentry.PrinterWidgetViewModel
import com.yedc.android.instancemanagement.InstancesDataService
import com.yedc.android.instancemanagement.autosend.AutoSendSettingsProvider
import com.yedc.android.projects.ProjectsDataService
import com.yedc.android.utilities.ChangeLockProvider
import com.yedc.android.utilities.FormsRepositoryProvider
import com.yedc.android.utilities.InstancesRepositoryProvider
import com.yedc.android.utilities.MediaUtils
import com.yedc.android.utilities.SavepointsRepositoryProvider
import com.yedc.async.Scheduler
import com.yedc.audiorecorder.recording.AudioRecorder
import com.yedc.permissions.PermissionsChecker
import com.yedc.permissions.PermissionsProvider
import com.yedc.printer.HtmlPrinter
import com.yedc.qrcode.zxing.QRCodeCreator
import com.yedc.settings.SettingsProvider
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
    private val fusedLocationClient: com.yedc.location.LocationClient,
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
            _root_ide_package_.com.yedc.android.formentry.FormEntryViewModel::class.java -> _root_ide_package_.com.yedc.android.formentry.FormEntryViewModel(
                System::currentTimeMillis,
                scheduler,
                formSessionRepository,
                sessionId,
                formsRepositoryProvider.create(projectId),
                changeLockProvider.create(projectId)
            )

            _root_ide_package_.com.yedc.android.formentry.saving.FormSaveViewModel::class.java -> {
                _root_ide_package_.com.yedc.android.formentry.saving.FormSaveViewModel(
                    handle,
                    System::currentTimeMillis,
                    _root_ide_package_.com.yedc.android.formentry.saving.DiskFormSaver(),
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

            _root_ide_package_.com.yedc.android.formentry.BackgroundAudioViewModel::class.java -> {
                val recordAudioActionRegistry =
                    if (mode == _root_ide_package_.com.yedc.android.formentry.FormOpeningMode.VIEW_SENT) {
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

                _root_ide_package_.com.yedc.android.formentry.BackgroundAudioViewModel(
                    audioRecorder,
                    settingsProvider.getUnprotectedSettings(),
                    recordAudioActionRegistry,
                    permissionsChecker,
                    System::currentTimeMillis,
                    formSessionRepository.get(sessionId)
                )
            }

            _root_ide_package_.com.yedc.android.formentry.backgroundlocation.BackgroundLocationViewModel::class.java -> {
                val locationManager =
                    _root_ide_package_.com.yedc.android.formentry.backgroundlocation.BackgroundLocationManager(
                        fusedLocationClient,
                        _root_ide_package_.com.yedc.android.formentry.backgroundlocation.BackgroundLocationHelper(
                            permissionsProvider,
                            settingsProvider.getUnprotectedSettings(),
                            formSessionRepository,
                            sessionId
                        )
                    )

                _root_ide_package_.com.yedc.android.formentry.backgroundlocation.BackgroundLocationViewModel(
                    locationManager
                )
            }

            _root_ide_package_.com.yedc.android.formentry.audit.IdentityPromptViewModel::class.java -> _root_ide_package_.com.yedc.android.formentry.audit.IdentityPromptViewModel()

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
