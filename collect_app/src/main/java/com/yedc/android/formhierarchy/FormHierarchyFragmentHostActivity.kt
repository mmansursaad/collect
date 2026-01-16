package com.yedc.android.formhierarchy

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.yedc.analytics.Analytics
import com.yedc.android.R
import com.yedc.android.activities.FormEntryViewModelFactory
import com.yedc.android.entities.EntitiesRepositoryProvider
import com.yedc.android.formentry.FormOpeningMode
import com.yedc.android.formentry.FormSessionRepository
import com.yedc.android.injection.DaggerUtils
import com.yedc.android.instancemanagement.InstancesDataService
import com.yedc.android.instancemanagement.autosend.AutoSendSettingsProvider
import com.yedc.android.projects.ProjectsDataService
import com.yedc.android.utilities.ChangeLockProvider
import com.yedc.android.utilities.FormsRepositoryProvider
import com.yedc.android.utilities.InstancesRepositoryProvider
import com.yedc.android.utilities.MediaUtils
import com.yedc.android.utilities.SavepointsRepositoryProvider
import com.yedc.androidshared.ui.FragmentFactoryBuilder
import com.yedc.async.Scheduler
import com.yedc.audiorecorder.recording.AudioRecorder
import com.yedc.permissions.PermissionsChecker
import com.yedc.permissions.PermissionsProvider
import com.yedc.printer.HtmlPrinter
import com.yedc.qrcode.zxing.QRCodeCreatorImpl
import com.yedc.settings.SettingsProvider
import com.yedc.strings.localization.LocalizedActivity
import javax.inject.Inject

class FormHierarchyFragmentHostActivity : LocalizedActivity() {

    @Inject
    lateinit var scheduler: Scheduler

    @Inject
    lateinit var formSessionRepository: FormSessionRepository

    @Inject
    lateinit var mediaUtils: MediaUtils

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var audioRecorder: AudioRecorder

    @Inject
    lateinit var projectsDataService: ProjectsDataService

    @Inject
    lateinit var entitiesRepositoryProvider: EntitiesRepositoryProvider

    @Inject
    lateinit var permissionsChecker: PermissionsChecker

    @Inject
    lateinit var fusedLocationClient: com.yedc.location.LocationClient

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    @Inject
    lateinit var autoSendSettingsProvider: AutoSendSettingsProvider

    @Inject
    lateinit var instancesRepositoryProvider: InstancesRepositoryProvider

    @Inject
    lateinit var formsRepositoryProvider: FormsRepositoryProvider

    @Inject
    lateinit var savepointsRepositoryProvider: SavepointsRepositoryProvider

    @Inject
    lateinit var instancesDataService: InstancesDataService

    @Inject
    lateinit var changeLockProvider: ChangeLockProvider

    private val sessionId by lazy { intent.getStringExtra(EXTRA_SESSION_ID)!! }
    private val viewModelFactory by lazy {
        FormEntryViewModelFactory(
            this,
            FormOpeningMode.EDIT_SAVED,
            sessionId,
            scheduler,
            formSessionRepository,
            mediaUtils,
            audioRecorder,
            projectsDataService,
            entitiesRepositoryProvider,
            settingsProvider,
            permissionsChecker,
            fusedLocationClient,
            permissionsProvider,
            autoSendSettingsProvider,
            formsRepositoryProvider,
            instancesRepositoryProvider,
            savepointsRepositoryProvider,
            QRCodeCreatorImpl(),
            HtmlPrinter(),
            instancesDataService,
            changeLockProvider
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        DaggerUtils.getComponent(this).inject(this)

        val viewOnly = intent.getBooleanExtra(EXTRA_VIEW_ONLY, false)
        supportFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(_root_ide_package_.com.yedc.android.formhierarchy.FormHierarchyFragment::class) {
                _root_ide_package_.com.yedc.android.formhierarchy.FormHierarchyFragment(
                    viewOnly,
                    viewModelFactory,
                    this,
                    scheduler,
                    instancesDataService,
                    projectsDataService.getCurrentProject().value!!.uuid
                )
            }
            .forClass(_root_ide_package_.com.yedc.android.formentry.repeats.DeleteRepeatDialogFragment::class) {
                _root_ide_package_.com.yedc.android.formentry.repeats.DeleteRepeatDialogFragment(
                    viewModelFactory
                )
            }
            .build()

        if (formSessionRepository.get(sessionId).value == null) {
            super.onCreate(null)
            finish()
            return
        } else {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.hierarchy_host_layout)

            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController

            val shouldShowNewEditMessage = intent.getBooleanExtra(SHOW_NEW_EDIT_MESSAGE, false)
            navController.setGraph(
                R.navigation.form_entry,
                FormHierarchyFragmentArgs.Builder(shouldShowNewEditMessage)
                    .build()
                    .toBundle()
            )

            setSupportActionBar(findViewById(com.yedc.androidshared.R.id.toolbar))
        }
    }

    companion object {
        const val EXTRA_SESSION_ID = "session_id"
        const val EXTRA_VIEW_ONLY = "view_only"
        const val SHOW_NEW_EDIT_MESSAGE = "show_new_edit_message"
    }
}
