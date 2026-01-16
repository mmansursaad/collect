package com.yedc.android.feature.projects

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import com.yedc.android.support.TestDependencies
import com.yedc.android.support.rules.CollectTestRule
import com.yedc.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class LaunchScreenTest {

    private val rule = CollectTestRule(false)
    private val testDependencies = TestDependencies()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain(testDependencies).around(rule)

    @Test
    fun clickingTryCollectAtLaunch_setsAppUpWithDemoProject() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .openProjectSettingsDialog()
            .assertCurrentProject("Demo project", "demo.getodk.org")
            .clickSettings()
            .clickServerSettings()
            .clickOnURL()
            .assertText("https://demo.getodk.org")
    }

    @Test
    fun clickingManuallyEnterProjectDetails_andAddingProjectDetails_setsAppUpWithProjectDetails() {
        rule.startAtFirstLaunch()
            .clickManuallyEnterProjectDetails()
            .inputUrl("https://my-server.com")
            .inputUsername("John")
            .addProject()
            .assertProjectIcon("M")
            .openProjectSettingsDialog()
            .assertCurrentProject("my-server.com", "John / my-server.com")
    }

    @Test
    fun clickingAutomaticallyEnterProjectDetails_andScanningQRCode_setsAppUpWithProjectDetails() {
        val page = rule.startAtFirstLaunch()
            .clickConfigureWithQrCode()

        testDependencies.fakeBarcodeScannerViewFactory.scan("{\"general\":{\"server_url\":\"https:\\/\\/my-server.com\",\"username\":\"adam\",\"password\":\"1234\"},\"admin\":{}}")
        page.checkIsToastWithMessageDisplayed(com.yedc.strings.R.string.switched_project, "my-server.com")

        com.yedc.android.support.pages.MainMenuPage()
            .assertOnPage()
            .openProjectSettingsDialog()
            .assertCurrentProject("my-server.com", "adam / my-server.com")
    }

    @Test
    fun whenThereAreProjects_goesToMainMenu() {
        rule.withProject("https://example.com")
        rule.relaunch(com.yedc.android.support.pages.MainMenuPage())
    }
}
