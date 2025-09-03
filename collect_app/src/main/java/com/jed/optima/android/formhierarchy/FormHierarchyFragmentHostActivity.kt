package com.jed.optima.android.formhierarchy

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.jed.optima.analytics.Analytics
import com.jed.optima.android.R
import com.jed.optima.android.activities.FormEntryViewModelFactory
import com.jed.optima.android.entities.EntitiesRepositoryProvider
import com.jed.optima.android.formentry.FormOpeningMode
import com.jed.optima.android.formentry.FormSessionRepository
import com.jed.optima.android.injection.DaggerUtils
import com.jed.optima.android.instancemanagement.InstancesDataService
import com.jed.optima.android.instancemanagement.autosend.AutoSendSettingsProvider
import com.jed.optima.android.projects.ProjectsDataService
import com.jed.optima.android.utilities.ChangeLockProvider
import com.jed.optima.android.utilities.FormsRepositoryProvider
import com.jed.optima.android.utilities.InstancesRepositoryProvider
import com.jed.optima.android.utilities.MediaUtils
import com.jed.optima.android.utilities.SavepointsRepositoryProvider
import com.jed.optima.androidshared.ui.FragmentFactoryBuilder
import com.jed.optima.async.Scheduler
import com.jed.optima.audiorecorder.recording.AudioRecorder
import com.jed.optima.permissions.PermissionsChecker
import com.jed.optima.permissions.PermissionsProvider
import com.jed.optima.printer.HtmlPrinter
import com.jed.optima.qrcode.zxing.QRCodeCreatorImpl
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.strings.localization.LocalizedActivity
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
    lateinit var fusedLocationClient: com.jed.optima.location.LocationClient

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
            .forClass(com.jed.optima.android.formhierarchy.FormHierarchyFragment::class) {
                com.jed.optima.android.formhierarchy.FormHierarchyFragment(
                    viewOnly,
                    viewModelFactory,
                    this,
                    scheduler,
                    instancesDataService,
                    projectsDataService.getCurrentProject().value!!.uuid
                )
            }
            .forClass(com.jed.optima.android.formentry.repeats.DeleteRepeatDialogFragment::class) {
                com.jed.optima.android.formentry.repeats.DeleteRepeatDialogFragment(viewModelFactory)
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

            setSupportActionBar(findViewById(com.jed.optima.androidshared.R.id.toolbar))
        }
    }

    companion object {
        const val EXTRA_SESSION_ID = "session_id"
        const val EXTRA_VIEW_ONLY = "view_only"
        const val SHOW_NEW_EDIT_MESSAGE = "show_new_edit_message"
    }
}
