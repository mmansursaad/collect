package com.yedc.android.projects

import android.app.Application
import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import com.yedc.android.application.initialization.AnalyticsInitializer
import com.yedc.android.application.initialization.MapsInitializer
import com.yedc.android.mainmenu.MainMenuActivity
import com.yedc.android.support.Matchers.isPasswordHidden
import com.yedc.fragmentstest.FragmentScenarioLauncherRule
import com.yedc.projects.Project
import com.yedc.projects.ProjectCreator
import com.yedc.projects.ProjectsRepository
import com.yedc.settings.ODKAppSettingsImporter
import com.yedc.settings.SettingsProvider
import org.robolectric.shadows.ShadowToast

@RunWith(AndroidJUnit4::class)
class ManualProjectCreatorDialogTest {

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    @Test
    fun `Password should be protected`() {
        val scenario = launcherRule.launch(ManualProjectCreatorDialog::class.java)
        scenario.onFragment {
            onView(withHint(com.yedc.strings.R.string.server_url)).inRoot(isDialog())
                .perform(replaceText("123456789"))
            onView(withHint(com.yedc.strings.R.string.server_url)).inRoot(isDialog())
                .check(matches(not(isPasswordHidden())))

            onView(withHint(com.yedc.strings.R.string.username)).inRoot(isDialog()).perform(replaceText("123456789"))
            onView(withHint(com.yedc.strings.R.string.username)).inRoot(isDialog())
                .check(matches(not(isPasswordHidden())))

            onView(withHint(com.yedc.strings.R.string.password)).inRoot(isDialog()).perform(replaceText("123456789"))
            onView(withHint(com.yedc.strings.R.string.password)).inRoot(isDialog())
                .check(matches(isPasswordHidden()))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on the 'Cancel' button`() {
        val scenario = launcherRule.launch(ManualProjectCreatorDialog::class.java)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
            onView(withText(com.yedc.strings.R.string.cancel)).inRoot(isDialog()).perform(click())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on a device back button`() {
        val scenario = launcherRule.launch(ManualProjectCreatorDialog::class.java)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
            onView(isRoot()).perform(pressBack())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun `The 'Add' button should be disabled when url is blank`() {
        val scenario = launcherRule.launch(ManualProjectCreatorDialog::class.java)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))

            onView(withText(com.yedc.strings.R.string.add)).inRoot(isDialog()).perform(click())
            assertThat(it.isVisible, `is`(true))

            onView(withHint(com.yedc.strings.R.string.server_url)).inRoot(isDialog()).perform(replaceText(" "))
            onView(withText(com.yedc.strings.R.string.add)).inRoot(isDialog()).perform(click())
            assertThat(it.isVisible, `is`(true))
        }
    }

    @Test
    fun `When URL has no protocol, a toast is displayed`() {
        val scenario = launcherRule.launch(ManualProjectCreatorDialog::class.java)
        scenario.onFragment {
            onView(withHint(com.yedc.strings.R.string.server_url)).inRoot(isDialog())
                .perform(replaceText("demo.getodk.org"))
            onView(withText(com.yedc.strings.R.string.add)).inRoot(isDialog()).perform(click())
            assertThat(it.isVisible, `is`(true))

            val toastText = ShadowToast.getTextOfLatestToast()
            assertThat(toastText, `is`(it.getString(com.yedc.strings.R.string.url_error)))
        }
    }

    @Test
    fun `Server project creation should be triggered after clicking on the 'Add' button`() {
        val projectCreator = mock<ProjectCreator> {}
        val projectsDataService = mock<ProjectsDataService> {
            on { requireCurrentProject() } doReturn Project.DEMO_PROJECT
        }

        com.yedc.android.support.CollectHelpers.overrideAppDependencyModule(object : _root_ide_package_.com.yedc.android.injection.config.AppDependencyModule() {
            override fun providesProjectCreator(
                projectsRepository: ProjectsRepository,
                projectsDataService: ProjectsDataService,
                settingsImporter: ODKAppSettingsImporter,
                settingsProvider: SettingsProvider
            ): ProjectCreator {
                return projectCreator
            }

            override fun providesCurrentProjectProvider(
                application: Application,
                settingsProvider: SettingsProvider,
                projectsRepository: ProjectsRepository,
                analyticsInitializer: AnalyticsInitializer,
                context: Context,
                mapsInitializer: MapsInitializer
            ): ProjectsDataService {
                return projectsDataService
            }
        })

        val scenario = launcherRule.launch(ManualProjectCreatorDialog::class.java)
        scenario.onFragment {
            onView(withHint(com.yedc.strings.R.string.server_url)).inRoot(isDialog())
                .perform(replaceText("https://my-server.com"))
            onView(withHint(com.yedc.strings.R.string.username)).inRoot(isDialog()).perform(replaceText("adam"))
            onView(withHint(com.yedc.strings.R.string.password)).inRoot(isDialog()).perform(replaceText("1234"))

            onView(withText(com.yedc.strings.R.string.add)).inRoot(isDialog()).perform(click())
            verify(projectCreator).createNewProject("{\"general\":{\"server_url\":\"https:\\/\\/my-server.com\",\"username\":\"adam\",\"password\":\"1234\"},\"admin\":{},\"project\":{}}", true)
        }
    }

    @Test
    fun `Server project creation goes to main menu`() {
        val scenario = launcherRule.launch(ManualProjectCreatorDialog::class.java)
        scenario.onFragment {
            onView(withHint(com.yedc.strings.R.string.server_url)).inRoot(isDialog())
                .perform(replaceText("https://my-server.com"))

            Intents.init()
            onView(withText(com.yedc.strings.R.string.add)).inRoot(isDialog()).perform(click())
            Intents.intended(IntentMatchers.hasComponent(MainMenuActivity::class.java.name))
            Intents.release()
        }
    }
}
