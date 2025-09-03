package com.jed.optima.android.feature.formmanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import com.jed.optima.android.support.SubmissionParser
import com.jed.optima.android.support.TestDependencies
import com.jed.optima.android.support.pages.AccessControlPage
import com.jed.optima.android.support.pages.FormEntryPage.QuestionAndAnswer
import com.jed.optima.android.support.pages.SaveOrDiscardFormDialog
import com.jed.optima.android.support.rules.CollectTestRule
import com.jed.optima.android.support.rules.RecentAppsRule
import com.jed.optima.android.support.rules.TestRuleChain
import com.jed.optima.strings.R.plurals
import com.jed.optima.strings.R.string

@RunWith(AndroidJUnit4::class)
class BulkFinalizationTest {

    private val testDependencies = TestDependencies()
    private val recentAppsRule = RecentAppsRule()
    private val rule = CollectTestRule(useDemoProject = false)

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(recentAppsRule)
        .around(rule)

    @Test
    fun canBulkFinalizeDraftsInTheListOfDrafts() {
        rule.withProject("http://example.com")
            .copyForm("one-question.xml", "example.com")
            .startBlankForm("One Question")
            .fillOutAndSave(QuestionAndAnswer("what is your age", "97"))
            .startBlankForm("One Question")
            .fillOutAndSave(QuestionAndAnswer("what is your age", "98"))

            .clickDrafts(2)
            .clickFinalizeAll(2)
            .clickFinalize()
            .checkIsSnackbarWithQuantityDisplayed(plurals.bulk_finalize_success, 2)
            .assertTextDoesNotExist("One Question")
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())

            .assertNumberOfFinalizedForms(2)
    }

    @Test
    fun canNotBulkFinalizeDraftsInTheListOfSentForms() {
        rule.withProject("http://example.com")
            .copyForm("one-question.xml", "example.com")
            .startBlankForm("One Question")
            .fillOutAndSave(QuestionAndAnswer("what is your age", "97"))
            .clickViewSentForm(0)
            .assertNoOptionsMenu()
    }

    @Test
    fun whenThereAreDraftsWithConstraintViolations_marksFormsAsHavingErrors() {
        rule.withProject("http://example.com")
            .copyForm("two-question-required.xml", "example.com")
            .startBlankForm("Two Question Required")
            .fillOut(QuestionAndAnswer("What is your name?", "Dan"))
            .pressBack(SaveOrDiscardFormDialog(com.jed.optima.android.support.pages.MainMenuPage()))
            .clickSaveChanges()

            .startBlankForm("Two Question Required")
            .fillOutAndSave(
                QuestionAndAnswer("What is your name?", "Tim"),
                QuestionAndAnswer("What is your age?", "45", true)
            )

            .clickDrafts(2)
            .clickFinalizeAll(2)
            .clickFinalize()
            .checkIsSnackbarWithMessageDisplayed(string.bulk_finalize_partial_success, 1, 1)
            .assertText("Two Question Required")
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())

            .assertNumberOfEditableForms(1)
            .assertNumberOfFinalizedForms(1)
    }

    @Test
    fun whenADraftPreviouslyHadConstraintViolations_marksFormsAsHavingErrors() {
        rule.withProject("http://example.com")
            .copyForm("two-question-required.xml", "example.com")
            .startBlankForm("Two Question Required")
            .fillOut(QuestionAndAnswer("What is your name?", "Dan"))
            .pressBack(SaveOrDiscardFormDialog(com.jed.optima.android.support.pages.MainMenuPage()))
            .clickSaveChanges()

            .clickDrafts(1)
            .clickFinalizeAll(1)
            .clickFinalize()
            .checkIsSnackbarWithQuantityDisplayed(plurals.bulk_finalize_failure, 1)

            .clickOptionsIcon(string.finalize_all_drafts)
            .clickOnString(string.finalize_all_drafts)
            .clickOnTextInDialog(string.finalize,
                com.jed.optima.android.support.pages.EditSavedFormPage()
            )
            .checkIsSnackbarWithQuantityDisplayed(plurals.bulk_finalize_failure, 1)
    }

    @Test
    fun doesNotFinalizeInstancesWithSavePoints() {
        rule.withProject("http://example.com")
            .copyForm("one-question.xml", "example.com")
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickSaveAsDraft()

            .clickDrafts()
            .clickOnForm("One Question")
            .killAndReopenApp(rule, recentAppsRule,
                com.jed.optima.android.support.pages.MainMenuPage()
            )

            .clickDrafts()
            .clickFinalizeAll(1)
            .clickFinalize()
            .checkIsSnackbarWithMessageDisplayed(string.bulk_finalize_unsupported, 0)
            .assertText("One Question")
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())

            .assertNumberOfEditableForms(1)
            .assertNumberOfFinalizedForms(0)
    }

    @Test
    fun doesNotFinalizeInstancesFromEncryptedForms() {
        rule.withProject("http://example.com")
            .copyForm("encrypted.xml", "example.com")
            .startBlankForm("encrypted")
            .swipeToEndScreen()
            .clickSaveAsDraft()

            .clickDrafts(1)
            .clickFinalizeAll(1)
            .clickFinalize()
            .checkIsSnackbarWithMessageDisplayed(string.bulk_finalize_unsupported, 0)
            .assertText("encrypted")
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())

            .assertNumberOfEditableForms(1)
            .assertNumberOfFinalizedForms(0)
    }

    @Test
    fun doesNotFinalizeAlreadyFinalizedInstances() {
        rule.withProject("http://example.com")
            .copyForm("one-question.xml", "example.com")
            .startBlankForm("One Question")
            .fillOutAndSave(QuestionAndAnswer("what is your age", "97"))
            .startBlankForm("One Question")
            .fillOutAndFinalize(QuestionAndAnswer("what is your age", "98"))

            .clickDrafts(1)
            .clickFinalizeAll(1)
            .clickFinalize()
            .checkIsSnackbarWithQuantityDisplayed(plurals.bulk_finalize_success, 1)
            .assertTextDoesNotExist("One Question")
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())

            .assertNumberOfFinalizedForms(2)
    }

    @Test
    fun whenAutoSendIsEnabled_draftsAreSentAfterFinalizing() {
        val mainMenuPage = rule.withProject(testDependencies.server.url)
            .enableAutoSend(
                testDependencies.scheduler,
                string.wifi_cellular_autosend
            )

            .copyForm("one-question.xml", testDependencies.server.hostName)
            .startBlankForm("One Question")
            .fillOutAndSave(QuestionAndAnswer("what is your age", "97"))

            .clickDrafts(1)
            .clickFinalizeAll(1)
            .clickFinalize()
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())

        testDependencies.scheduler.runDeferredTasks()

        mainMenuPage.clickViewSentForm(1)
            .assertText("One Question")

        assertThat(testDependencies.server.submissions.size, equalTo(1))
    }

    @Test
    fun whenDraftFormHasAutoSendEnabled_draftsAreSentAfterFinalizing() {
        val mainMenuPage = rule.withProject(testDependencies.server.url)
            .copyForm("one-question-autosend.xml", testDependencies.server.hostName)
            .startBlankForm("One Question Autosend")
            .fillOutAndSave(QuestionAndAnswer("what is your age", "97"))

            .clickDrafts(1)
            .clickFinalizeAll(1)
            .clickFinalize()
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())

        testDependencies.scheduler.runDeferredTasks()

        mainMenuPage.clickViewSentForm(1)
            .assertText("One Question Autosend")

        assertThat(testDependencies.server.submissions.size, equalTo(1))
    }

    @Test
    fun canCancel() {
        rule.withProject("http://example.com")
            .copyForm("one-question.xml", "example.com")
            .startBlankForm("One Question")
            .fillOutAndSave(QuestionAndAnswer("what is your age", "97"))

            .clickDrafts(1)
            .clickFinalizeAll(1)
            .clickCancel()
            .assertText("One Question")
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())

            .assertNumberOfEditableForms(1)
    }

    @Test
    fun canBeDisabled() {
        rule.withProject("http://example.com")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickAccessControl()
            .clickFormEntrySettings()
            .clickOnString(string.finalize_all_drafts)
            .pressBack(AccessControlPage())
            .pressBack(com.jed.optima.android.support.pages.ProjectSettingsPage())
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())

            .copyForm("one-question.xml", "example.com")
            .startBlankForm("One Question")
            .fillOutAndSave(QuestionAndAnswer("what is your age", "1892"))
            .clickDrafts()
            .assertNoOptionsMenu()
    }

    @Test
    fun finalizingFinalizedEditViaBulkFinalize_savesFormWithCorrectInstanceIdAndDeprecatedId() {
        rule.withProject(testDependencies.server.url)
            .copyForm("one-question-editable.xml", testDependencies.server.hostName)
            .startBlankForm("One Question Editable")
            .swipeToEndScreen()
            .clickFinalize()

            .clickSendFinalizedForm(1)
            .clickOnForm("One Question Editable")
            .editForm("One Question Editable")
            .clickOnQuestion("what is your age")
            .swipeToEndScreen("One Question Editable (Edit 1)")
            .clickSaveAsDraft()

            .clickDrafts(1)
            .clickFinalizeAll(1)
            .clickFinalize()
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())

            .clickSendFinalizedForm(2)
            .clickSelectAll()
            .clickSendSelected()

        val (firstFormInstanceID, firstFormDeprecatedID) = SubmissionParser.getMetaIds(testDependencies.server.submissions[0])
        val (secondFormInstanceID, secondFormDeprecatedID) = SubmissionParser.getMetaIds(testDependencies.server.submissions[1])

        assertThat(firstFormDeprecatedID, equalTo(null))
        assertThat(firstFormInstanceID, equalTo(secondFormDeprecatedID))
        assertThat(secondFormInstanceID, not(firstFormInstanceID))
    }
}
