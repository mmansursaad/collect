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
class InstancePickActionTest {

    private val rule = CollectTestRule()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain()
        .around(rule)

    @Test
    fun pickInstance_andTheSelectingInstance_returnsInstanceUri() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickSaveAsDraft()

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = com.jed.optima.android.external.InstancesContract.CONTENT_TYPE
        val result = rule.launchForResult(intent,
            com.jed.optima.android.support.pages.EditSavedFormPage()
        ) {
            it.clickOnFormClosingApp("One Question")
        }

        val instanceId = ContentProviderUtils.getInstanceDatabaseId("DEMO", "one_question")
        assertThat(
            result.resultData.data,
            equalTo(com.jed.optima.android.external.InstancesContract.getUri("DEMO", instanceId))
        )
    }
}
