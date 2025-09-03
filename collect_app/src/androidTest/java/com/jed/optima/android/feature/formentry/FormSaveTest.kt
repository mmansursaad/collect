package com.jed.optima.android.feature.formentry

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import com.jed.optima.android.support.TestDependencies
import com.jed.optima.android.support.rules.CollectTestRule
import com.jed.optima.android.support.rules.TestRuleChain

class FormSaveTest {
    private val rule = CollectTestRule()
    private val testDependencies = TestDependencies()

    @get:Rule
    val copyFormChain: RuleChain = TestRuleChain.chain(testDependencies).around(rule)

    @Test
    fun whenBlankFormSavedAsDraft_displaySnackbarWithEditAction() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion(0, "25")
            .swipeToEndScreen()
            .clickSaveAsDraft()
            .assertText(com.jed.optima.strings.R.string.form_saved_as_draft)
            .clickOnString(com.jed.optima.strings.R.string.edit_form)
            .assertText("25")
            .assertText(com.jed.optima.strings.R.string.jump_to_beginning)
            .assertText(com.jed.optima.strings.R.string.jump_to_end)
    }

    @Test
    fun whenDraftFinalized_displaySnackbarWithViewAction() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion(0, "25")
            .swipeToEndScreen()
            .clickSaveAsDraft()
            .clickDrafts()
            .clickOnForm("One Question")
            .clickGoToEnd()
            .clickFinalize()
            .assertText(com.jed.optima.strings.R.string.form_saved)
            .clickOnString(com.jed.optima.strings.R.string.view_form)
            .assertText("25")
            .assertTextDoesNotExist(com.jed.optima.strings.R.string.jump_to_beginning)
            .assertTextDoesNotExist(com.jed.optima.strings.R.string.jump_to_end)
            .assertText(com.jed.optima.strings.R.string.exit)
    }

    @Test
    fun snackbarCanBeDismissed_andWillNotBeDisplayedAgainAfterRecreatingTheActivity() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickSaveAsDraft()
            .assertText(com.jed.optima.strings.R.string.form_saved_as_draft)
            .closeSnackbar()
            .assertTextDoesNotExist(com.jed.optima.strings.R.string.form_saved_as_draft)
            .rotateToLandscape(com.jed.optima.android.support.pages.MainMenuPage())
            .assertTextDoesNotExist(com.jed.optima.strings.R.string.form_saved_as_draft)
    }

    @Test
    fun whenFormDeletedDuringFilling_displayErrorOnAttemptToSave() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .setServer(testDependencies.server.url)
            .enableMatchExactly()
            .startBlankForm("One Question")
            .also { testDependencies.scheduler.runDeferredTasks() }
            .clickSaveWithError("Sorry, form save failed! Form can't be found.")
            .swipeToEndScreen()
            .clickSaveAsDraftWithError("Sorry, form save failed! Form can't be found.")
            .clickFinalizeWithError("Sorry, form save failed! Form can't be found.")
    }
}
