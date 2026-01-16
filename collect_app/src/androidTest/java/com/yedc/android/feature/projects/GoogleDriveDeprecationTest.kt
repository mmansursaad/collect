package com.yedc.android.feature.projects

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import com.yedc.android.injection.DaggerUtils
import com.yedc.android.support.TestDependencies
import com.yedc.android.support.rules.CollectTestRule
import com.yedc.android.support.rules.TestRuleChain
import com.yedc.androidtest.RecordedIntentsRule
import com.yedc.projects.Project

class GoogleDriveDeprecationTest {
    private val rule = CollectTestRule()
    private val testDependencies = TestDependencies()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(RecordedIntentsRule())
        .around(rule)

    @Test
    fun bannerIsNotVisibleInNonGoogleDriveProjects() {
        rule
            .startAtMainMenu()
            .assertTextDoesNotExist(com.yedc.strings.R.string.google_drive_removed_message)
    }

    @Test
    fun bannerIsVisibleInGoogleDriveProjects() {
        addProject(Project.Saved("1", "Old GD project", "A", "#ffffff", true))

        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .selectProject("Old GD project")
            .assertText(com.yedc.strings.R.string.google_drive_removed_message)
    }

    @Test
    fun forumThreadIsOpenedAfterClickingLearnMore() {
        addProject(Project.Saved("1", "Old GD project", "A", "#ffffff", true))

        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .selectProject("Old GD project")
            .clickOnString(com.yedc.strings.R.string.learn_more_button_text)

        intended(
            allOf(
                hasComponent(com.yedc.webpage.WebViewActivity::class.java.name),
                hasExtra("url", "https://forum.getodk.org/t/40097")
            )
        )
    }

    @Test
    fun reconfiguringShouldBeVisibleInNonGoogleDriveProjects() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .assertText(com.yedc.strings.R.string.reconfigure_with_qr_code_settings_title)
    }

    private fun addProject(project: Project): Project.Saved {
        val component =
            DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Application>())
        return component.projectsRepository().save(project)
    }
}
