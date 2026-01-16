package com.yedc.android.activities

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.DialogFragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.yedc.android.external.FormUriActivity
import com.yedc.android.formhierarchy.FormHierarchyFragmentHostActivity
import com.yedc.android.formmanagement.FormFillingIntentFactory
import com.yedc.android.support.CollectHelpers.resetProcess
import com.yedc.androidshared.ui.DialogFragmentUtils
import com.yedc.androidtest.ActivityScenarioExtensions.isFinishing
import com.yedc.androidtest.ActivityScenarioLauncherRule
import com.yedc.androidtest.RecordedIntentsRule
import com.yedc.async.Scheduler
import com.yedc.externalapp.ExternalAppUtils
import com.yedc.formstest.FormFixtures.form
import com.yedc.strings.R
import com.yedc.testshared.ActivityControllerRule
import com.yedc.testshared.AssertIntentsHelper
import com.yedc.testshared.Assertions.assertVisible
import com.yedc.testshared.FakeScheduler
import com.yedc.testshared.Interactions
import com.yedc.testshared.RobolectricHelpers.recreateWithProcessRestore
import org.robolectric.Shadows.shadowOf
import java.io.File

@RunWith(AndroidJUnit4::class)
class FormFillingActivityTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val recordedIntentsRule = RecordedIntentsRule()

    @get:Rule
    val activityControllerRule = ActivityControllerRule()

    @get:Rule
    val scenarioLauncherRule = ActivityScenarioLauncherRule()

    private val assertIntentsHelper = AssertIntentsHelper()

    private val scheduler = FakeScheduler()
    private val dependencies = object : _root_ide_package_.com.yedc.android.injection.config.AppDependencyModule() {
        override fun providesScheduler(workManager: WorkManager): Scheduler {
            return scheduler
        }
    }

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var component: _root_ide_package_.com.yedc.android.injection.config.AppDependencyComponent

    @Before
    fun setup() {
        component = com.yedc.android.support.CollectHelpers.overrideAppDependencyModule(dependencies)
    }

    @Test
    fun whenProcessIsKilledAndRestored_returnsToHierarchyAtQuestion() {
        val projectId = com.yedc.android.support.CollectHelpers.setupDemoProject()

        val form = setupForm("forms/two-question.xml")
        val intent = FormFillingIntentFactory.newFormIntent(
            application,
            _root_ide_package_.com.yedc.android.external.FormsContract.getUri(projectId, form!!.dbId),
            _root_ide_package_.com.yedc.android.activities.FormFillingActivity::class
        )

        // Start activity
        val initial = activityControllerRule.build(_root_ide_package_.com.yedc.android.activities.FormFillingActivity::class.java, intent).setup()
        scheduler.flush()
        assertVisible(withText("Two Question"))
        assertVisible(withText("What is your name?"))

        Interactions.clickOn(withText(R.string.form_forward))
        scheduler.flush()
        assertVisible(withText("What is your age?"))

        // Recreate and assert we start FormHierarchyFragmentHostActivity
        val recreated = activityControllerRule.add {
            initial.recreateWithProcessRestore { resetProcess(dependencies) }
        }

        scheduler.flush()
        assertIntentsHelper.assertNewIntent(FormHierarchyFragmentHostActivity::class)

        // Return to FormFillingActivity from FormHierarchyFragmentHostActivity
        val hierarchyIntent = shadowOf(recreated.get()).nextStartedActivityForResult.intent
        shadowOf(recreated.get()).receiveResult(hierarchyIntent, Activity.RESULT_CANCELED, null)
        scheduler.flush()

        assertVisible(withText("Two Question"))
        assertVisible(withText("What is your age?"))
    }

    @Test
    fun whenProcessIsKilledAndRestored_andHierarchyIsOpen_returnsToHierarchyAtQuestion() {
        val projectId = com.yedc.android.support.CollectHelpers.setupDemoProject()

        val form = setupForm("forms/two-question.xml")
        val intent = FormFillingIntentFactory.newFormIntent(
            application,
            _root_ide_package_.com.yedc.android.external.FormsContract.getUri(projectId, form!!.dbId),
            _root_ide_package_.com.yedc.android.activities.FormFillingActivity::class
        )

        // Start activity
        val initial = activityControllerRule.build(_root_ide_package_.com.yedc.android.activities.FormFillingActivity::class.java, intent).setup()
        scheduler.flush()
        assertVisible(withText("Two Question"))
        assertVisible(withText("What is your name?"))

        Interactions.clickOn(withText(R.string.form_forward))
        scheduler.flush()
        assertVisible(withText("What is your age?"))

        Interactions.clickOn(withContentDescription(R.string.view_hierarchy))
        assertIntentsHelper.assertNewIntent(FormHierarchyFragmentHostActivity::class)

        // Recreate and assert we start FormHierarchyFragmentHostActivity
        val recreated = activityControllerRule.add {
            initial.recreateWithProcessRestore { resetProcess(dependencies) }
        }

        scheduler.flush()
        assertIntentsHelper.assertNewIntent(FormHierarchyFragmentHostActivity::class)

        // Return to FormFillingActivity from FormHierarchyFragmentHostActivity
        val hierarchyIntent = shadowOf(recreated.get()).nextStartedActivityForResult.intent
        shadowOf(recreated.get()).receiveResult(hierarchyIntent, Activity.RESULT_CANCELED, null)
        scheduler.flush()

        assertVisible(withText("Two Question"))
        assertVisible(withText("What is your age?"))
    }

    @Test
    fun whenProcessIsKilledAndRestored_andThereADialogFragmentOpen_doesNotRestoreDialogFragment() {
        val projectId = com.yedc.android.support.CollectHelpers.setupDemoProject()

        val form = setupForm("forms/two-question.xml")
        val intent = FormFillingIntentFactory.newFormIntent(
            application,
            _root_ide_package_.com.yedc.android.external.FormsContract.getUri(projectId, form!!.dbId),
            _root_ide_package_.com.yedc.android.activities.FormFillingActivity::class
        )

        // Start activity
        val initial = activityControllerRule.build(_root_ide_package_.com.yedc.android.activities.FormFillingActivity::class.java, intent).setup()
        scheduler.flush()
        assertVisible(withText("Two Question"))
        assertVisible(withText("What is your name?"))

        Interactions.clickOn(withText(R.string.form_forward))
        scheduler.flush()
        assertVisible(withText("What is your age?"))

        val initialFragmentManager = initial.get().supportFragmentManager
        DialogFragmentUtils.showIfNotShowing(TestDialogFragment::class.java, initialFragmentManager)
        assertThat(
            initialFragmentManager.fragments.any { it::class == TestDialogFragment::class },
            equalTo(true)
        )

        // Recreate and assert we start FormHierarchyFragmentHostActivity
        val recreated = activityControllerRule.add {
            initial.recreateWithProcessRestore { resetProcess(dependencies) }
        }

        scheduler.flush()
        assertIntentsHelper.assertNewIntent(FormHierarchyFragmentHostActivity::class)

        // Return to FormFillingActivity from FormHierarchyFragmentHostActivity
        val hierarchyIntent = shadowOf(recreated.get()).nextStartedActivityForResult.intent
        shadowOf(recreated.get()).receiveResult(hierarchyIntent, Activity.RESULT_CANCELED, null)
        scheduler.flush()

        assertVisible(withText("Two Question"))
        assertVisible(withText("What is your age?"))
    }

    @Test
    fun whenProcessIsKilledAndRestored_andIsWaitingForExternalData_dataCanStillBeReturned() {
        val projectId = com.yedc.android.support.CollectHelpers.setupDemoProject()

        val form = setupForm("forms/two-question-external.xml")
        val intent = FormFillingIntentFactory.newFormIntent(
            application,
            _root_ide_package_.com.yedc.android.external.FormsContract.getUri(projectId, form!!.dbId),
            _root_ide_package_.com.yedc.android.activities.FormFillingActivity::class
        )

        // Start activity
        val initial = activityControllerRule.build(_root_ide_package_.com.yedc.android.activities.FormFillingActivity::class.java, intent).setup()
        scheduler.flush()
        assertVisible(withText("Two Question"))
        assertVisible(withText("What is your name?"))

        Interactions.clickOn(withText(R.string.form_forward))
        scheduler.flush()
        assertVisible(withText("What is your age?"))

        // Open external app
        Interactions.clickOn(withContentDescription(R.string.launch_app))
        assertIntentsHelper.assertNewIntent(hasAction("com.example.EXAMPLE"))

        // Recreate with result
        val returnData = ExternalAppUtils.getReturnIntent("159")
        activityControllerRule.add {
            initial.recreateWithProcessRestore(RESULT_OK, returnData) { resetProcess(dependencies) }
        }

        scheduler.flush()

        assertIntentsHelper.assertNoNewIntent()
        assertVisible(withText("Two Question"))
        assertVisible(withText("What is your age?"))
        assertVisible(withText("159"))
    }

    /**
     * This case will usually be protected by [FormUriActivity], but it could be possible when
     * restoring the app/backstack.
     */
    @Test
    fun whenFormDoesNotExist_showsFatalError() {
        val projectId = com.yedc.android.support.CollectHelpers.setupDemoProject()

        val intent = FormFillingIntentFactory.newFormIntent(
            application,
            _root_ide_package_.com.yedc.android.external.FormsContract.getUri(projectId, 101),
            _root_ide_package_.com.yedc.android.activities.FormFillingActivity::class
        )

        val scenario = scenarioLauncherRule.launch<_root_ide_package_.com.yedc.android.activities.FormFillingActivity>(intent)
        scheduler.flush()
        assertVisible(
            withText("This form no longer exists, please email support@getodk.org with a description of what you were doing when this happened."),
            root = isDialog()
        )

        Interactions.clickOn(withText(R.string.ok), root = isDialog())
        assertThat(scenario.isFinishing, equalTo(true))
    }

    private fun setupForm(testFormPath: String): com.yedc.forms.Form? {
        val formsDir = component.storagePathProvider().getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.FORMS)
        val formFile = _root_ide_package_.com.yedc.android.utilities.FileUtils.copyFileFromResources(
            testFormPath,
            File(formsDir, "two-question.xml")
        )

        val formsRepository = component.formsRepositoryProvider().create()
        val form = formsRepository.save(form(formFilePath = formFile.absolutePath))
        return form
    }

    class TestDialogFragment : DialogFragment()
}
