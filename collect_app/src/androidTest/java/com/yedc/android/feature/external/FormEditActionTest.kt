package com.yedc.android.feature.external

import android.content.Intent
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import com.yedc.android.support.ContentProviderUtils
import com.yedc.android.support.pages.AppClosedPage
import com.yedc.android.support.rules.CollectTestRule
import com.yedc.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class FormEditActionTest {

    private val rule = CollectTestRule()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain()
        .around(rule)

    @Test
    fun editForm_andThenFillingForm_returnsNewInstanceURI() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .clickFillBlankForm() // Sync form

        val formId = ContentProviderUtils.getFormDatabaseId("DEMO", "one_question")
        val uri = _root_ide_package_.com.yedc.android.external.FormsContract.getUri("DEMO", formId)

        val formIntent = Intent(Intent.ACTION_EDIT).also { it.data = uri }
        val result = rule.launchForResult(formIntent,
            com.yedc.android.support.pages.FormEntryPage("One Question")
        ) {
            it.answerQuestion("what is your age", "31")
                .swipeToEndScreen()
                .clickFinalize(AppClosedPage())
        }

        val instanceId = ContentProviderUtils.getInstanceDatabaseId("DEMO", "one_question")
        assertThat(result.resultData.data, equalTo(_root_ide_package_.com.yedc.android.external.InstancesContract.getUri("DEMO", instanceId)))
    }
}
