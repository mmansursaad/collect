package com.jed.optima.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import com.jed.optima.android.support.rules.CollectTestRule
import com.jed.optima.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class CatchFormDesignExceptionsTest {

    private val rule = CollectTestRule()

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain().around(rule)

    @Test // https://github.com/getodk/collect/issues/4750
    fun whenFormHasFatalErrors_explanationDialogShouldBeDisplayedAndSurviveActivityRecreationAndTheFormShouldBeClosedAfterClickingOK() {
        rule.startAtMainMenu()
            .copyForm("form_design_error.xml")
            .clickFillBlankForm()
            .clickOnForm("Relevance and calculate loop")
            .answerQuestion(1, "B")
            .assertText("third")
            .answerQuestion(2, "C")
            // Answering C above triggers a recomputation round which resets fullName to name.
            // They're then equal which makes the third question non-relevant. Trying to change the
            // value of a non-relevant node throws an exception.
            .answerQuestion(2, "D")
            .assertTextInDialog(com.jed.optima.strings.R.string.update_widgets_error)
            .rotateToLandscape(com.jed.optima.android.support.pages.OkDialog())
            .assertTextInDialog(com.jed.optima.strings.R.string.update_widgets_error)
            .clickOKOnDialog()
            .assertOnPage(com.jed.optima.android.support.pages.MainMenuPage())
    }

    @Test
    fun whenFormHasNonFatalErrors_explanationDialogShouldBeDisplayedAndTheFormShouldNotBeClosedAfterClickingOK() {
        rule.startAtMainMenu()
            .copyForm("g6Error.xml")
            .startBlankFormWithError("g6Error", false)
            .clickOK(com.jed.optima.android.support.pages.FormEntryPage("g6Error"))
    }

    @Test
    fun whenFormHasNonFatalErrors_explanationDialogShouldNotSurviveActivityRecreation() {
        rule.startAtMainMenu()
            .copyForm("g6Error.xml")
            .startBlankFormWithError("g6Error", false)
            .clickOK(com.jed.optima.android.support.pages.FormEntryPage("g6Error"))
            .rotateToLandscape(com.jed.optima.android.support.pages.FormEntryPage("g6Error"))
            .assertTextDoesNotExist(com.jed.optima.strings.R.string.error_occured)
    }

    @Test
    fun typeMismatchErrorMessage_shouldBeDisplayedWhenItOccurs() {
        rule.startAtMainMenu()
            .copyForm("validate.xml")
            .startBlankForm("validate")
            .longPressOnQuestion("year")
            .removeResponse()
            .swipeToNextQuestionWithError(false)
            .checkIsTextDisplayedOnDialog("The value \"-01-01\" can't be converted to a date.")
    }
}
