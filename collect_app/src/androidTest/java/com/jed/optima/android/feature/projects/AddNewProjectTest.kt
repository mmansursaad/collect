package com.jed.optima.android.feature.projects

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import com.jed.optima.android.support.TestDependencies
import com.jed.optima.android.support.rules.CollectTestRule
import com.jed.optima.android.support.rules.TestRuleChain
import com.jed.optima.androidtest.RecordedIntentsRule

class AddNewProjectTest {

    val rule = CollectTestRule()
    private val testDependencies = TestDependencies()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(RecordedIntentsRule())
        .around(rule)

    @Test
    fun addingProjectManually_addsNewProject() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()
            .switchToManualMode()
            .inputUrl("https://my-server.com")
            .inputUsername("John")
            .addProject()

            .openProjectSettingsDialog()
            .assertCurrentProject("my-server.com", "John / my-server.com")
            .assertInactiveProject("Demo project", "demo.getodk.org")
    }

    @Test
    fun addingProjectFromQrCode_addsNewProject() {
        val page = rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()

        testDependencies.fakeBarcodeScannerViewFactory.scan("{\"general\":{\"server_url\":\"https:\\/\\/my-server.com\",\"username\":\"adam\",\"password\":\"1234\"},\"admin\":{}}")
        page.checkIsToastWithMessageDisplayed(com.jed.optima.strings.R.string.switched_project, "my-server.com")

        com.jed.optima.android.support.pages.MainMenuPage()
            .assertOnPage()
            .openProjectSettingsDialog()
            .assertCurrentProject("my-server.com", "adam / my-server.com")
            .assertInactiveProject("Demo project", "demo.getodk.org")
    }

    @Test
    fun switchesToExistingProject_whenDuplicateProjectScanned_andOptionToSwitchToExistingSelected() {
        val page = rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()

        testDependencies.fakeBarcodeScannerViewFactory.scan("{\"general\":{\"server_url\":\"https://demo.getodk.org\"},\"admin\":{}}")

        page.assertDuplicateDialogShown()
            .switchToExistingProject()
            .checkIsToastWithMessageDisplayed(com.jed.optima.strings.R.string.switched_project, "Demo project")
            .openProjectSettingsDialog()
            .assertCurrentProject("Demo project", "demo.getodk.org")
            .assertNotInactiveProject("Demo project")
    }

    @Test
    fun addsDuplicateProject_whenDuplicateProjectScanned_andOptionToAddDuplicateProjectSelected() {
        val page = rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()

        testDependencies.fakeBarcodeScannerViewFactory.scan("{\"general\":{\"server_url\":\"https://demo.getodk.org\"},\"admin\":{}}")

        page.assertDuplicateDialogShown()
            .addDuplicateProject()
            .checkIsToastWithMessageDisplayed(com.jed.optima.strings.R.string.switched_project, "Demo project")
            .openProjectSettingsDialog()
            .assertCurrentProject("Demo project", "demo.getodk.org")
            .assertInactiveProject("Demo project", "demo.getodk.org")
    }

    @Test
    fun switchesToExistingProject_whenDuplicateProjectEnteredManually_andOptionToSwitchToExistingSelected() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()
            .switchToManualMode()
            .inputUrl("https://demo.getodk.org")
            .addProjectAndAssertDuplicateDialogShown()
            .switchToExistingProject()
            .checkIsToastWithMessageDisplayed(com.jed.optima.strings.R.string.switched_project, "Demo project")
            .openProjectSettingsDialog()
            .assertCurrentProject("Demo project", "demo.getodk.org")
            .assertNotInactiveProject("Demo project")
    }

    @Test
    fun addsDuplicateProject_whenDuplicateProjectEnteredManually_andOptionToAddDuplicateProjectSelected() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()
            .switchToManualMode()
            .inputUrl("https://demo.getodk.org")
            .addProjectAndAssertDuplicateDialogShown()
            .addDuplicateProject()
            .checkIsToastWithMessageDisplayed(com.jed.optima.strings.R.string.switched_project, "Demo project")
            .openProjectSettingsDialog()
            .assertCurrentProject("Demo project", "demo.getodk.org")
            .assertInactiveProject("Demo project", "demo.getodk.org")
    }
}
