package com.yedc.android.feature.instancemanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import com.yedc.android.R
import com.yedc.android.support.TestDependencies
import com.yedc.android.support.pages.SendFinalizedFormPage
import com.yedc.android.support.rules.CollectTestRule
import com.yedc.android.support.rules.TestRuleChain.chain
import com.yedc.strings.R.string

@RunWith(AndroidJUnit4::class)
class DeleteSavedFormTest {

    private val rule = CollectTestRule(useDemoProject = false)
    private val testDependencies = TestDependencies()

    @get:Rule
    val chain: RuleChain = chain(testDependencies).around(rule)

    @Test
    fun deletingAForm_removesFormFromFinalizedForms() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "30")
            .swipeToEndScreen()
            .clickFinalize()

            .clickDeleteSavedForm()
            .clickForm("One Question")
            .clickDeleteSelected(1)
            .clickDeleteForms()
            .checkIsSnackbarWithMessageDisplayed(string.file_deleted_ok, 1)
            .assertTextDoesNotExist("One Question")
            .pressBack(com.yedc.android.support.pages.MainMenuPage())
            .assertNumberOfFinalizedForms(0)
    }

    @Test
    fun whenFinalizedButNotSentFormHasCreatedALocalEntity_doesNotAppearInListToDelete() {
        testDependencies.server.addForm("one-question-entity-registration.xml")

        rule.withMatchExactlyProject(testDependencies.server.url)
            // Drafts can be deleted
            .startBlankForm("One Question Entity Registration")
            .fillOutAndSave(com.yedc.android.support.pages.FormEntryPage.QuestionAndAnswer("Name", "Logan Roy"))
            .clickDeleteSavedForm()
            .assertText("One Question Entity Registration")
            .pressBack(com.yedc.android.support.pages.MainMenuPage())

            // Finalized forms can not be deleted
            .clickDrafts(1)
            .clickOnForm("One Question Entity Registration")
            .clickGoToEnd()
            .clickFinalize()
            .clickDeleteSavedForm()
            .assertTextDoesNotExist("One Question Entity Registration")
            .pressBack(com.yedc.android.support.pages.MainMenuPage())

            // Sent forms can be deleted
            .clickSendFinalizedForm(1)
            .clickSelectAll()
            .clickSendSelected()
            .clickOK(SendFinalizedFormPage())
            .pressBack(com.yedc.android.support.pages.MainMenuPage())
            .clickDeleteSavedForm()
            .assertText("One Question Entity Registration")
            .pressBack(com.yedc.android.support.pages.MainMenuPage())
    }

    @Test
    fun accessingSortMenuInDeleteSavedInstancesShouldNotCrashTheAppAfterRotatingTheDevice() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "30")
            .swipeToEndScreen()
            .clickFinalize()
            .clickDeleteSavedForm()
            .rotateToLandscape(com.yedc.android.support.pages.DeleteSavedFormPage())
            .clickOnId(R.id.menu_sort)
            .assertText(string.sort_by)
    }
}
