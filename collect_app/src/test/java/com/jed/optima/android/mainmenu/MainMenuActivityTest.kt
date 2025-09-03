package com.jed.optima.android.mainmenu

import android.app.Application
import android.content.RestrictionsManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.jed.optima.android.R
import com.jed.optima.android.activities.CrashHandlerActivity
import com.jed.optima.android.activities.DeleteFormsActivity
import com.jed.optima.android.application.initialization.AnalyticsInitializer
import com.jed.optima.android.fakes.FakePermissionsProvider
import com.jed.optima.android.formentry.FormOpeningMode
import com.jed.optima.android.formlists.blankformlist.BlankFormListActivity
import com.jed.optima.android.instancemanagement.InstancesDataService
import com.jed.optima.android.instancemanagement.autosend.AutoSendSettingsProvider
import com.jed.optima.android.projects.ProjectsDataService
import com.jed.optima.android.utilities.FormsRepositoryProvider
import com.jed.optima.android.utilities.InstancesRepositoryProvider
import com.jed.optima.androidshared.system.BroadcastReceiverRegister
import com.jed.optima.androidtest.ActivityScenarioLauncherRule
import com.jed.optima.async.Scheduler
import com.jed.optima.crashhandler.CrashHandler
import com.jed.optima.mobiledevicemanagement.MDMConfigObserver
import com.jed.optima.permissions.PermissionsChecker
import com.jed.optima.permissions.PermissionsProvider
import com.jed.optima.projects.Project
import com.jed.optima.projects.ProjectCreator
import com.jed.optima.projects.ProjectsRepository
import com.jed.optima.settings.ODKAppSettingsImporter
import com.jed.optima.settings.SettingsProvider
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class MainMenuActivityTest {

    private val project = Project.Saved("123", "Project", "P", "#f5f5f5")

    private val mainMenuViewModel = mock<MainMenuViewModel> {
        on { sendableInstancesCount } doReturn MutableLiveData(0)
        on { sentInstancesCount } doReturn MutableLiveData(0)
        on { editableInstancesCount } doReturn MutableLiveData(0)
        on { savedForm } doReturn MutableLiveData()
    }

    private val currentProjectViewModel = mock<CurrentProjectViewModel> {
        on { hasCurrentProject() } doReturn true
        on { currentProject } doReturn MutableLiveData(project)
    }

    private val permissionsViewModel = mock<RequestPermissionsViewModel> {
        on { shouldAskForPermissions() } doReturn false
    }

    private val permissionsProvider = FakePermissionsProvider()

    private val application = ApplicationProvider.getApplicationContext<Application>()

    @get:Rule
    val launcherRule = ActivityScenarioLauncherRule()

    @Before
    fun setup() {
        com.jed.optima.android.support.CollectHelpers.overrideAppDependencyModule(object : com.jed.optima.android.injection.config.AppDependencyModule() {
            override fun providesMainMenuViewModelFactory(
                versionInformation: com.jed.optima.android.version.VersionInformation,
                application: Application,
                settingsProvider: SettingsProvider,
                instancesDataService: InstancesDataService,
                scheduler: Scheduler,
                projectsDataService: ProjectsDataService,
                analyticsInitializer: AnalyticsInitializer,
                permissionChecker: PermissionsChecker,
                formsRepositoryProvider: FormsRepositoryProvider,
                instancesRepositoryProvider: InstancesRepositoryProvider,
                autoSendSettingsProvider: AutoSendSettingsProvider
            ): MainMenuViewModelFactory {
                return object : MainMenuViewModelFactory(
                    versionInformation,
                    application,
                    settingsProvider,
                    instancesDataService,
                    scheduler,
                    projectsDataService,
                    permissionChecker,
                    formsRepositoryProvider,
                    instancesRepositoryProvider,
                    autoSendSettingsProvider
                ) {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return when (modelClass) {
                            MainMenuViewModel::class.java -> mainMenuViewModel
                            CurrentProjectViewModel::class.java -> currentProjectViewModel
                            RequestPermissionsViewModel::class.java -> permissionsViewModel
                            else -> throw IllegalArgumentException()
                        } as T
                    }
                }
            }

            override fun providesPermissionsProvider(permissionsChecker: PermissionsChecker?): PermissionsProvider {
                return permissionsProvider
            }

            override fun providesMDMConfigObserver(
                scheduler: Scheduler,
                settingsProvider: SettingsProvider,
                projectsRepository: ProjectsRepository,
                projectCreator: ProjectCreator,
                settingsImporter: ODKAppSettingsImporter,
                broadcastReceiverRegister: BroadcastReceiverRegister,
                restrictionsManager: RestrictionsManager
            ): MDMConfigObserver {
                return mock<MDMConfigObserver>()
            }
        })

        CrashHandler.install(application)
    }

    @After
    fun teardown() {
        CrashHandler.uninstall(application)
    }

    @Test
    fun `Activity title is current project name`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity {
            assertThat(it.title, `is`("Project"))
        }
    }

    @Test
    fun `Project icon for current project should be displayed`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val projectIcon = activity.findViewById<TextView>(R.id.project_icon_text)

            assertThat(projectIcon.visibility, `is`(View.VISIBLE))
            assertThat(projectIcon.text, `is`(project.icon))

            val background = projectIcon.background as GradientDrawable
            assertThat(background.color!!.defaultColor, equalTo(Color.parseColor(project.color)))
        }
    }

    @Test
    fun `Fill Blank Form button should have proper text`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val button = activity.findViewById<StartNewFormButton>(R.id.enter_data)
            assertThat(button.text, `is`(activity.getString(com.jed.optima.strings.R.string.enter_data)))
        }
    }

    @Test
    fun `Fill Blank Form button should start list of blank forms`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            Intents.init()

            val button = activity.findViewById<StartNewFormButton>(R.id.enter_data)
            button.performClick()
            assertThat(
                Intents.getIntents()[0],
                hasComponent(BlankFormListActivity::class.java.name)
            )

            Intents.release()
        }
    }

    @Test
    fun `Edit Saved Form button should have proper text`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val button = activity.findViewById<MainMenuButton>(R.id.review_data)
            assertThat(button.text, `is`(activity.getString(com.jed.optima.strings.R.string.review_data)))
        }
    }

    @Test
    fun `Edit Saved Form button should start list of saved forms`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            Intents.init()

            val button = activity.findViewById<MainMenuButton>(R.id.review_data)
            button.performClick()
            assertThat(Intents.getIntents()[0], hasComponent(com.jed.optima.android.activities.InstanceChooserList::class.java.name))
            assertThat(
                Intents.getIntents()[0].extras!!.get(FormOpeningMode.FORM_MODE_KEY),
                `is`(FormOpeningMode.EDIT_SAVED)
            )

            Intents.release()
        }
    }

    @Test
    fun `Send Finalized Form button should have proper text`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val button = activity.findViewById<MainMenuButton>(R.id.send_data)
            assertThat(button.text, `is`(activity.getString(com.jed.optima.strings.R.string.send_data)))
        }
    }

    @Test
    fun `Send Finalized Form button should start list of finalized forms`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            Intents.init()

            val button = activity.findViewById<MainMenuButton>(R.id.send_data)
            button.performClick()
            assertThat(
                Intents.getIntents()[0],
                hasComponent(com.jed.optima.android.instancemanagement.send.InstanceUploaderListActivity::class.java.name)
            )

            Intents.release()
        }
    }

    @Test
    fun `View Sent Form button should have proper text`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val button = activity.findViewById<MainMenuButton>(R.id.view_sent_forms)
            assertThat(button.text, `is`(activity.getString(com.jed.optima.strings.R.string.view_sent_forms)))
        }
    }

    @Test
    fun `View Sent Form button should start list of sent forms`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            Intents.init()

            val button = activity.findViewById<MainMenuButton>(R.id.view_sent_forms)
            button.performClick()
            assertThat(Intents.getIntents()[0], hasComponent(com.jed.optima.android.activities.InstanceChooserList::class.java.name))
            assertThat(
                Intents.getIntents()[0].extras!!.get(FormOpeningMode.FORM_MODE_KEY),
                `is`(FormOpeningMode.VIEW_SENT)
            )

            Intents.release()
        }
    }

    @Test
    fun `Get Blank Form button should have proper text`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val button = activity.findViewById<MainMenuButton>(R.id.get_forms)
            assertThat(button.text, `is`(activity.getString(com.jed.optima.strings.R.string.get_forms)))
        }
    }

    @Test
    fun `Get Blank Form button should start list of forms to download`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            Intents.init()

            val button = activity.findViewById<MainMenuButton>(R.id.get_forms)
            button.performClick()
            assertThat(
                Intents.getIntents()[0],
                hasComponent(com.jed.optima.android.activities.FormDownloadListActivity::class.java.name)
            )

            Intents.release()
        }
    }

    @Test
    fun `Delete Saved Form button should have proper text`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val button = activity.findViewById<MainMenuButton>(R.id.manage_forms)
            assertThat(button.text, `is`(activity.getString(com.jed.optima.strings.R.string.manage_files)))
        }
    }

    @Test
    fun `Delete Saved Form button should start list of forms to delete`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            Intents.init()

            val button = activity.findViewById<MainMenuButton>(R.id.manage_forms)
            button.performClick()
            assertThat(
                Intents.getIntents()[0],
                hasComponent(DeleteFormsActivity::class.java.name)
            )

            Intents.release()
        }
    }

    @Test
    fun `When editSavedFormButton is enabled in settings, should be visible`() {
        whenever(mainMenuViewModel.shouldEditSavedFormButtonBeVisible()).thenReturn(true)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val editSavedFormButton = activity.findViewById<MainMenuButton>(R.id.review_data)
            assertThat(editSavedFormButton.visibility, equalTo(View.VISIBLE))
        }
    }

    @Test
    fun `When editSavedFormButton is disabled in settings, should be gone`() {
        whenever(mainMenuViewModel.shouldEditSavedFormButtonBeVisible()).thenReturn(false)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val editSavedFormButton = activity.findViewById<MainMenuButton>(R.id.review_data)
            assertThat(editSavedFormButton.visibility, equalTo(View.GONE))
        }
    }

    @Test
    fun `When sendFinalizedFormButton is enabled in settings, should be visible`() {
        whenever(mainMenuViewModel.shouldSendFinalizedFormButtonBeVisible()).thenReturn(true)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val editSavedFormButton = activity.findViewById<MainMenuButton>(R.id.send_data)
            assertThat(editSavedFormButton.visibility, equalTo(View.VISIBLE))
        }
    }

    @Test
    fun `When sendFinalizedFormButton is disabled in settings, should be gone`() {
        whenever(mainMenuViewModel.shouldSendFinalizedFormButtonBeVisible()).thenReturn(false)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val editSavedFormButton = activity.findViewById<MainMenuButton>(R.id.send_data)
            assertThat(editSavedFormButton.visibility, equalTo(View.GONE))
        }
    }

    @Test
    fun `When viewSentFormButton is enabled in settings, should be visible`() {
        whenever(mainMenuViewModel.shouldViewSentFormButtonBeVisible()).thenReturn(true)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val editSavedFormButton = activity.findViewById<MainMenuButton>(R.id.view_sent_forms)
            assertThat(editSavedFormButton.visibility, equalTo(View.VISIBLE))
        }
    }

    @Test
    fun `When viewSentFormButton is disabled in settings, should be gone`() {
        whenever(mainMenuViewModel.shouldViewSentFormButtonBeVisible()).thenReturn(false)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val editSavedFormButton = activity.findViewById<MainMenuButton>(R.id.view_sent_forms)
            assertThat(editSavedFormButton.visibility, equalTo(View.GONE))
        }
    }

    @Test
    fun `When getBlankFormButton is enabled in settings, should be visible`() {
        whenever(mainMenuViewModel.shouldGetBlankFormButtonBeVisible()).thenReturn(true)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val editSavedFormButton = activity.findViewById<MainMenuButton>(R.id.get_forms)
            assertThat(editSavedFormButton.visibility, equalTo(View.VISIBLE))
        }
    }

    @Test
    fun `When getBlankFormButton is disabled in settings, should be gone`() {
        whenever(mainMenuViewModel.shouldGetBlankFormButtonBeVisible()).thenReturn(false)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val editSavedFormButton = activity.findViewById<MainMenuButton>(R.id.get_forms)
            assertThat(editSavedFormButton.visibility, equalTo(View.GONE))
        }
    }

    @Test
    fun `When deleteSavedFormButton is enabled in settings, should be visible`() {
        whenever(mainMenuViewModel.shouldDeleteSavedFormButtonBeVisible()).thenReturn(true)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val editSavedFormButton = activity.findViewById<MainMenuButton>(R.id.manage_forms)
            assertThat(editSavedFormButton.visibility, equalTo(View.VISIBLE))
        }
    }

    @Test
    fun `When deleteSavedFormButton is disabled in settings, should be gone`() {
        whenever(mainMenuViewModel.shouldDeleteSavedFormButtonBeVisible()).thenReturn(false)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val editSavedFormButton = activity.findViewById<MainMenuButton>(R.id.manage_forms)
            assertThat(editSavedFormButton.visibility, equalTo(View.GONE))
        }
    }

    @Test
    fun `when shouldAskForPermissions is true, shows permissions dialog`() {
        whenever(permissionsViewModel.shouldAskForPermissions()).doReturn(true)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity {
            val dialog =
                it.supportFragmentManager.findFragmentByTag(PermissionsDialogFragment::class.java.name)
            assertThat(dialog, notNullValue())
        }
    }

    @Test
    fun `when shouldAskForPermissions is false, does not show permissions dialog`() {
        whenever(permissionsViewModel.shouldAskForPermissions()).doReturn(false)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity {
            val dialog =
                it.supportFragmentManager.findFragmentByTag(PermissionsDialogFragment::class.java.name)
            assertThat(dialog, equalTo(null))
        }
    }

    @Test
    fun `when there has been a crash, opens CrashHandlerActivity and finishes`() {
        CrashHandler.getInstance(application)!!.registerCrash(application, IllegalStateException())

        Robolectric.buildActivity(MainMenuActivity::class.java).use {
            it.setup()

            val startedActivityName = shadowOf(it.get()).nextStartedActivity.component?.className
            assertThat(startedActivityName, equalTo(CrashHandlerActivity::class.qualifiedName))
            assertThat(it.get().isFinishing, equalTo(true))
        }
    }
}
