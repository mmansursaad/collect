package com.jed.optima.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import com.jed.optima.android.support.pages.AccessControlPage
import com.jed.optima.android.support.rules.CollectTestRule
import com.jed.optima.android.support.rules.TestRuleChain.chain
import com.jed.optima.strings.R.string

@RunWith(AndroidJUnit4::class)
class FormEndTest {
    private val rule = CollectTestRule()

    @get:Rule
    val copyFormChain: RuleChain = chain().around(rule)

    @Test
    fun fillingForm_andPressingFinalize_finalizesForm() {
        rule.startAtMainMenu()
            .copyForm(FORM)
            .assertNumberOfFinalizedForms(0)
            .startBlankForm("One Question")
            .fillOutAndFinalize(com.jed.optima.android.support.pages.FormEntryPage.QuestionAndAnswer("what is your age", "52"))
            .assertNumberOfEditableForms(0)
            .assertNumberOfFinalizedForms(1)
    }

    @Test
    fun fillingForm_andPressingSaveAsDraft_savesACompleteDraft() {
        rule.startAtMainMenu()
            .copyForm(FORM)
            .assertNumberOfFinalizedForms(0)
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickSaveAsDraft()
            .assertNumberOfFinalizedForms(0)

            .clickDrafts(1)
            .assertText(string.draft_no_errors)
            .assertTextDoesNotExist(string.draft_errors)
    }

    @Test
    fun fillingForm_andPressingSaveAsDraft_whenThereAreViolatedConstraints_savesAIncompleteDraft() {
        rule.startAtMainMenu()
            .copyForm("two-question-required.xml")
            .assertNumberOfFinalizedForms(0)
            .startBlankForm("Two Question Required")
            .clickGoToArrow()
            .clickGoToEnd()
            .clickSaveAsDraft()
            .assertNumberOfFinalizedForms(0)

            .clickDrafts(1)
            .assertText(string.draft_errors)
            .assertTextDoesNotExist(string.draft_no_errors)
    }

    @Test
    fun disablingSaveAsDraftInSettings_disablesItInTheEndScreen() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickAccessControl()
            .clickFormEntrySettings()
            .clickOnSaveAsDraftInFormEnd()
            .pressBack(AccessControlPage())
            .pressBack(com.jed.optima.android.support.pages.ProjectSettingsPage())
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())
            .copyForm(FORM)
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .assertTextDoesNotExist(string.save_as_draft)
    }

    @Test
    fun disablingFinalizeInSettings_disablesItInTheEndScreen() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickAccessControl()
            .clickFormEntrySettings()
            .clickOnString(string.finalize)
            .pressBack(AccessControlPage())
            .pressBack(com.jed.optima.android.support.pages.ProjectSettingsPage())
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())
            .copyForm(FORM)
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .assertTextDoesNotExist(string.finalize)
    }

    companion object {
        private const val FORM = "one-question.xml"
    }
}
