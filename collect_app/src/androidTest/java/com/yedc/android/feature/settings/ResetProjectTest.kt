package com.yedc.android.feature.settings

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import com.yedc.android.support.TestDependencies
import com.yedc.android.support.pages.AccessControlPage
import com.yedc.android.support.rules.CollectTestRule
import com.yedc.android.support.rules.TestRuleChain.chain
import com.yedc.strings.R

class ResetProjectTest {

    private val rule = CollectTestRule(useDemoProject = false)
    private val testDependencies = TestDependencies()

    @get:Rule
    val ruleChain: RuleChain = chain(testDependencies)
        .around(rule)

    @Test
    fun canResetBlankForms() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .copyForm("all-widgets.xml")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .clickOnResetProject()
            .assertDisabled(R.string.reset_settings_button_reset)
            .clickOnString(R.string.reset_blank_forms)
            .clickOnString(R.string.reset_settings_button_reset)
            .clickOKOnDialog(com.yedc.android.support.pages.MainMenuPage())
            .clickFillBlankForm()
            .assertTextDoesNotExist("All widgets")
    }

    @Test
    fun canResetSavedForms() {
        testDependencies.server.addForm("one-question.xml")

        rule.withMatchExactlyProject(testDependencies.server.url)
            .startBlankForm("One Question")
            .fillOutAndFinalize(com.yedc.android.support.pages.FormEntryPage.QuestionAndAnswer("what is your age", "34"))

            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .clickOnResetProject()
            .assertDisabled(R.string.reset_settings_button_reset)
            .clickOnString(R.string.reset_saved_forms)
            .clickOnString(R.string.reset_settings_button_reset)
            .clickOKOnDialog(com.yedc.android.support.pages.MainMenuPage())

            .clickDrafts()
            .assertTextDoesNotExist("One Question")
    }

    @Test
    fun canResetProtectedSettings() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickAccessControl()
            .openUserSettings()
            .uncheckServerOption()
            .pressBack(AccessControlPage())
            .pressBack(com.yedc.android.support.pages.ProjectSettingsPage())
            .pressBack(com.yedc.android.support.pages.MainMenuPage())
            .openProjectSettingsDialog()
            .clickSettings()
            .checkIfServerOptionIsNotDisplayed()
            .pressBack(com.yedc.android.support.pages.MainMenuPage())
            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .clickOnResetProject()
            .clickOnString(R.string.reset_settings)
            .clickOnString(R.string.reset_settings_button_reset)
            .clickOKOnDialog(com.yedc.android.support.pages.MainMenuPage())
            .openProjectSettingsDialog()
            .clickSettings()
            .checkIfServerOptionIsDisplayed()
    }

    @Test
    fun canResetUserInterfaceSettings() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickOnUserInterface()
            .clickOnLanguage()
            .clickOnSelectedLanguage("español")

            .openProjectSettingsDialog()
            .clickSettings()
            .clickOnUserInterface()
            .assertText("español")
            .pressBack(com.yedc.android.support.pages.ProjectSettingsPage())
            .pressBack(com.yedc.android.support.pages.MainMenuPage())

            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .clickOnResetProject()
            .clickOnString(R.string.reset_settings)
            .clickOnString(R.string.reset_settings_button_reset)
            .clickOKOnDialog(com.yedc.android.support.pages.MainMenuPage())
            .openProjectSettingsDialog()
            .clickSettings()
            .clickOnUserInterface()
            .assertText(R.string.use_device_language)
            .assertTextDoesNotExist("español")
    }

    @Test
    fun when_rotateScreen_should_resetDialogNotDisappear() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .clickOnResetProject()
            .assertText(R.string.reset_settings_dialog_title)
            .rotateToLandscape(com.yedc.android.support.pages.ResetApplicationDialog())
            .assertText(R.string.reset_settings_dialog_title)
    }
}
