package com.yedc.android.feature.external

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
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
class InstanceEditActionTest {

    private val rule = CollectTestRule()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain()
        .around(rule)

    @Test
    fun editingInstance_andSaving_returnsInstanceURI() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickSaveAsDraft()

        val instanceId = ContentProviderUtils.getInstanceDatabaseId("DEMO", "one_question")
        val uri = _root_ide_package_.com.yedc.android.external.InstancesContract.getUri("DEMO", instanceId)

        val intent = Intent(Intent.ACTION_EDIT).also { it.data = uri }
        val result = rule.launchForResult(intent,
            com.yedc.android.support.pages.FormHierarchyPage("One Question")
        ) {
            it.clickGoToStart()
                .answerQuestion("what is your age", "32")
                .swipeToEndScreen()
                .clickFinalize(AppClosedPage())
        }

        assertThat(result.resultData.data, equalTo(uri))
    }
}
