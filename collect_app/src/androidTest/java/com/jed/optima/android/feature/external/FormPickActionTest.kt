package com.jed.optima.android.feature.external

import android.content.Intent
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import com.jed.optima.android.support.ContentProviderUtils
import com.jed.optima.android.support.rules.CollectTestRule
import com.jed.optima.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class FormPickActionTest {

    private val rule = CollectTestRule()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain()
        .around(rule)

    @Test
    fun pickForm_andTheSelectingForm_returnsFormUri() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = com.jed.optima.android.external.FormsContract.CONTENT_TYPE
        val result = rule.launchForResult(intent,
            com.jed.optima.android.support.pages.FillBlankFormPage()
        ) {
            it.clickOnForm("One Question")
        }

        val formId = ContentProviderUtils.getFormDatabaseId("DEMO", "one_question")
        assertThat(result.resultData.data, equalTo(com.jed.optima.android.external.FormsContract.getUri("DEMO", formId)))
    }
}
