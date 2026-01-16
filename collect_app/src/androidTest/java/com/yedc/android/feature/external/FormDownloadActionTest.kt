package com.yedc.android.feature.external

import android.app.Activity
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import com.yedc.android.support.TestDependencies
import com.yedc.android.support.pages.AppClosedPage
import com.yedc.android.support.pages.FormsDownloadResultPage
import com.yedc.android.support.rules.CollectTestRule
import com.yedc.android.support.rules.TestRuleChain
import com.yedc.android.utilities.ApplicationConstants.BundleKeys.FORM_IDS
import com.yedc.android.utilities.ApplicationConstants.BundleKeys.PASSWORD
import com.yedc.android.utilities.ApplicationConstants.BundleKeys.SUCCESS_KEY
import com.yedc.android.utilities.ApplicationConstants.BundleKeys.URL
import com.yedc.android.utilities.ApplicationConstants.BundleKeys.USERNAME

@RunWith(AndroidJUnit4::class)
class FormDownloadActionTest {

    private val testDependencies = TestDependencies()
    private val rule = CollectTestRule(useDemoProject = false)

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(rule)

    @Test
    fun passingIds_downloadsFormsFromProjectServer_andReturnsSuccessResult() {
        testDependencies.server.addForm("One Question", "one_question", "1", "one-question.xml")
        testDependencies.server.addForm("Two Question", "two_question", "1", "two-question.xml")

        val intent = Intent("com.yedc.android.FORM_DOWNLOAD")
        intent.type = _root_ide_package_.com.yedc.android.external.FormsContract.CONTENT_TYPE
        intent.putExtra(FORM_IDS, arrayOf("one_question"))

        rule.withProject(testDependencies.server.url)
        val result = rule.launchForResult(intent, FormsDownloadResultPage()) {
            it.assertSuccess()
                .clickOK(AppClosedPage())
        }

        assertThat(result.resultCode, equalTo(Activity.RESULT_OK))
        assertThat(result.resultData.getBooleanExtra(SUCCESS_KEY, false), equalTo(true))
        assertThat(
            result.resultData.getSerializableExtra(FORM_IDS),
            equalTo(
                mapOf(
                    "one_question" to true
                )
            )
        )

        rule.relaunch(com.yedc.android.support.pages.MainMenuPage())
            .clickFillBlankForm()
            .assertFormExists("One Question")
            .assertFormDoesNotExist("Two Question")
    }

    @Test
    fun passingIds_andServerDetails_downloadsFormsFromServer_andReturnsSuccessResult() {
        testDependencies.server.setCredentials("Pete", "meyre")
        testDependencies.server.addForm("One Question", "one_question", "1", "one-question.xml")

        val intent = Intent("com.yedc.android.FORM_DOWNLOAD")
        intent.type = _root_ide_package_.com.yedc.android.external.FormsContract.CONTENT_TYPE
        intent.putExtra(FORM_IDS, arrayOf("one_question"))
        intent.putExtra(URL, testDependencies.server.url)
        intent.putExtra(USERNAME, "Pete")
        intent.putExtra(PASSWORD, "meyre")

        rule.withProject("https://server2.example.com")
        val result = rule.launchForResult(intent, FormsDownloadResultPage()) {
            it.assertSuccess()
                .clickOK(AppClosedPage())
        }

        assertThat(result.resultCode, equalTo(Activity.RESULT_OK))
        assertThat(result.resultData.getBooleanExtra(SUCCESS_KEY, false), equalTo(true))
        assertThat(
            result.resultData.getSerializableExtra(FORM_IDS),
            equalTo(
                mapOf(
                    "one_question" to true
                )
            )
        )

        rule.relaunch(com.yedc.android.support.pages.MainMenuPage())
            .clickFillBlankForm()
            .assertFormExists("One Question")
    }

    @Test
    fun passingIds_andServerDetails_whenThereIsAnAuthenticationError_allowsUserToReenterCredentials_andReturnsSuccessResult() {
        testDependencies.server.setCredentials("Pete", "meyre")
        testDependencies.server.addForm("One Question", "one_question", "1", "one-question.xml")

        val intent = Intent("com.yedc.android.FORM_DOWNLOAD")
        intent.type = _root_ide_package_.com.yedc.android.external.FormsContract.CONTENT_TYPE
        intent.putExtra(FORM_IDS, arrayOf("one_question"))
        intent.putExtra(URL, testDependencies.server.url)
        intent.putExtra(USERNAME, "wrong")
        intent.putExtra(PASSWORD, "wrong")

        rule.withProject("https://server2.example.com")
        val result = rule.launchForResult(intent,
            com.yedc.android.support.pages.ServerAuthDialog()
        ) {
            it.fillUsername("Pete")
                .fillPassword("meyre")
                .clickOK(FormsDownloadResultPage())
                .clickOK(AppClosedPage())
        }

        assertThat(result.resultCode, equalTo(Activity.RESULT_OK))
        assertThat(result.resultData.getBooleanExtra(SUCCESS_KEY, false), equalTo(true))
        assertThat(
            result.resultData.getSerializableExtra(FORM_IDS),
            equalTo(
                mapOf(
                    "one_question" to true
                )
            )
        )

        rule.relaunch(com.yedc.android.support.pages.MainMenuPage())
            .clickFillBlankForm()
            .assertFormExists("One Question")
    }
}
