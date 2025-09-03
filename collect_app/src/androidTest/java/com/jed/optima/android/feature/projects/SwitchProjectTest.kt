package com.jed.optima.android.feature.projects

import android.Manifest
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import com.jed.optima.android.support.StubOpenRosaServer.EntityListItem
import com.jed.optima.android.support.TestDependencies
import com.jed.optima.android.support.pages.EntitiesPage
import com.jed.optima.android.support.rules.CollectTestRule
import com.jed.optima.android.support.rules.TestRuleChain

class SwitchProjectTest {

    val rule = CollectTestRule()
    val testDependencies = TestDependencies()

    @get:Rule
    var chain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(GrantPermissionRule.grant(Manifest.permission.CAMERA))
        .around(rule)

    @Test
    fun canSwitchActiveProjectToAnotherInList() {
        // Add project Turtle nesting
        rule.startAtMainMenu()
            .assertProjectIcon("D")
            .openProjectSettingsDialog()
            .clickAddProject()
            .switchToManualMode()
            .inputUrl("https://my-server.com")
            .inputUsername("John")
            .addProject()

            // Switch to Turtle nesting
            .openProjectSettingsDialog()
            .assertCurrentProject("my-server.com", "John / my-server.com")
            .assertInactiveProject("Demo project", "demo.getodk.org")
            .selectProject("Demo project")
            .checkIsToastWithMessageDisplayed(com.jed.optima.strings.R.string.switched_project, "Demo project")
            .assertProjectIcon("D")
    }

    @Test
    fun switchingProject_switchesSettingsFormsInstancesAndEntities() {
        testDependencies.server.addForm(
            "One Question Entity Registration",
            "one-question-entity",
            "1",
            "one-question-entity-registration.xml"
        )
        testDependencies.server.addForm(
            "one-question-entity-update.xml",
            listOf(EntityListItem("people.csv"))
        )

        rule.startAtMainMenu()
            // Copy and fill form
            .copyForm("two-question.xml")
            .startBlankForm("Two Question")
            .swipeToNextQuestion("What is your age?")
            .swipeToEndScreen()
            .clickSaveAsDraft()
            .clickDrafts(1)
            .assertText("Two Question")
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())

            // Create and switch to new project
            .assertProjectIcon("D")
            .openProjectSettingsDialog()
            .clickAddProject()
            .switchToManualMode()
            .inputUrl("https://my-server.com")
            .inputUsername("John")
            .addProject()

            // Set server and download form
            .setServer(testDependencies.server.url)
            .clickGetBlankForm()
            .clickGetSelected()
            .clickOKOnDialog(com.jed.optima.android.support.pages.MainMenuPage())

            // Fill form
            .startBlankForm("One Question Entity Registration")
            .fillOutAndFinalize(com.jed.optima.android.support.pages.FormEntryPage.QuestionAndAnswer("Name", "Alice"))
            .clickSendFinalizedForm(1)
            .assertText("One Question Entity Registration")
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())

            .openEntityBrowser()
            .clickOnList("people")
            .assertEntity("Alice", "full_name: Alice")
            .pressBack(EntitiesPage())
            .pressBack(com.jed.optima.android.support.pages.ExperimentalPage())
            .pressBack(com.jed.optima.android.support.pages.ProjectSettingsPage())
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())

            // Switch back to first project
            .openProjectSettingsDialog()
            .selectProject("Demo project")

            // Check server
            .openProjectSettingsDialog()
            .clickSettings()
            .clickServerSettings()
            .clickOnURL()
            .assertText("https://demo.getodk.org")
            .clickOKOnDialog()
            .pressBack(com.jed.optima.android.support.pages.ProjectSettingsPage())
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())

            // Check forms
            .clickFillBlankForm()
            .assertFormExists("Two Question")
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())

            // Check instances
            .clickDrafts(1)
            .assertText("Two Question")
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())

            // Check entities
            .openEntityBrowser()
            .assertTextDoesNotExist("people")
    }
}
