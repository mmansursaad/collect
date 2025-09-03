package com.jed.optima.android.feature.external

import android.content.Intent
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import com.jed.optima.android.support.ContentProviderUtils
import com.jed.optima.android.support.pages.AppClosedPage
import com.jed.optima.android.support.rules.CollectTestRule
import com.jed.optima.android.support.rules.TestRuleChain

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
        val uri = com.jed.optima.android.external.FormsContract.getUri("DEMO", formId)

        val formIntent = Intent(Intent.ACTION_EDIT).also { it.data = uri }
        val result = rule.launchForResult(formIntent,
            com.jed.optima.android.support.pages.FormEntryPage("One Question")
        ) {
            it.answerQuestion("what is your age", "31")
                .swipeToEndScreen()
                .clickFinalize(AppClosedPage())
        }

        val instanceId = ContentProviderUtils.getInstanceDatabaseId("DEMO", "one_question")
        assertThat(result.resultData.data, equalTo(com.jed.optima.android.external.InstancesContract.getUri("DEMO", instanceId)))
    }
}
