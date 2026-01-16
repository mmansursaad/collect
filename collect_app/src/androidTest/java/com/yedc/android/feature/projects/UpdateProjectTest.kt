package com.yedc.android.feature.projects

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import com.yedc.android.support.TestDependencies
import com.yedc.android.support.rules.CollectTestRule
import com.yedc.android.support.rules.TestRuleChain

class UpdateProjectTest {

    val rule = CollectTestRule()

    private val testDependencies = TestDependencies()

    @get:Rule
    var chain: RuleChain = TestRuleChain
        .chain(testDependencies)
        .around(rule)

    @Test
    fun updateProjectTest() {
        rule.startAtMainMenu()
            .assertProjectIcon("D")
            .openProjectSettingsDialog()
            .assertCurrentProject("Demo project", "demo.getodk.org")
            .clickSettings()
            .clickProjectDisplay()
            .setProjectName("Project X")
            .assertFileWithProjectNameUpdated("Demo project", "Project X")
            .setProjectIcon("XY")
            .setProjectColor("cccccc")
            .pressBack(com.yedc.android.support.pages.ProjectSettingsPage())
            .pressBack(com.yedc.android.support.pages.MainMenuPage())
            .openProjectSettingsDialog()
            .clickSettings()
            .clickServerSettings()
            .clickServerUsername()
            .inputText("Anna")
            .clickOKOnDialog()
            .pressBack(com.yedc.android.support.pages.ProjectSettingsPage())
            .pressBack(com.yedc.android.support.pages.MainMenuPage())

            .assertProjectIcon("X")
            .openProjectSettingsDialog()
            .assertCurrentProject("Project X", "Anna / demo.getodk.org")
    }

    @Test
    fun updateProjectName_updatesProjectNameFileSanitizingIt() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectDisplay()
            .setProjectName("Project<>")
            .assertFileWithProjectNameUpdated("Demo project", "Project__")
            .setProjectName(":*Project<>")
            .assertFileWithProjectNameUpdated("Project__", "__Project__")
    }

    @Test // https://github.com/getodk/collect/issues/5902
    fun updatingProjectDetails_whenThereIsMoreThanOneProject_doesNotDuplicateInactiveProjectsOnTheList() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()

        testDependencies.fakeBarcodeScannerViewFactory.scan("{\"general\":{\"server_url\":\"https:\\/\\/my-server.com\",\"username\":\"adam\",\"password\":\"1234\"},\"admin\":{}}")

        com.yedc.android.support.pages.MainMenuPage()
            // assert there are two projects displayed
            .openProjectSettingsDialog()
            .assertCurrentProject("my-server.com", "adam / my-server.com")
            .assertInactiveProject("Demo project", "demo.getodk.org")

            // Update project icon
            .clickSettings()
            .clickProjectDisplay()
            .setProjectIcon("Z")
            .pressBack(com.yedc.android.support.pages.ProjectSettingsPage())
            .pressBack(com.yedc.android.support.pages.MainMenuPage())

            // assert there are two projects displayed
            .openProjectSettingsDialog()
            .assertCurrentProject("my-server.com", "adam / my-server.com")
            .assertInactiveProject("Demo project", "demo.getodk.org")
    }
}
