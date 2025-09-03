package com.jed.optima.android.feature.external

import android.content.Context
import android.content.Intent
import android.provider.BaseColumns._ID
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import com.jed.optima.android.support.TestDependencies
import com.jed.optima.android.support.rules.CollectTestRule
import com.jed.optima.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class InstanceUploadActionTest {

    private val rule = CollectTestRule()
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val testDependencies = TestDependencies()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(rule)

    @Test
    fun whenIntentIncludesURLExtra_instancesAreUploadedToThatURL() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .fillOutAndFinalize(com.jed.optima.android.support.pages.FormEntryPage.QuestionAndAnswer("what is your age", "34"))

        val instanceId =
            context.contentResolver.query(com.jed.optima.android.external.InstancesContract.getUri("DEMO"), null, null, null, null)
                .use {
                    it!!.moveToFirst()
                    it.getLong(it.getColumnIndex(_ID))
                }

        val intent = Intent("com.jed.optima.android.INSTANCE_UPLOAD")
        intent.type = com.jed.optima.android.external.InstancesContract.CONTENT_TYPE
        intent.putExtra(com.jed.optima.android.utilities.ApplicationConstants.BundleKeys.URL, testDependencies.server.url)
        intent.putExtra("instances", longArrayOf(instanceId))

        rule.launch(intent, com.jed.optima.android.support.pages.OkDialog())
            .assertTextInDialog("One Question - Success")
        assertThat(testDependencies.server.submissions.size, equalTo(1))
    }

    @Test
    fun whenInstanceDoesNotExist_showsError() {
        rule.startAtMainMenu()

        val intent = Intent("com.jed.optima.android.INSTANCE_UPLOAD")
        intent.type = com.jed.optima.android.external.InstancesContract.CONTENT_TYPE
        intent.putExtra("instances", longArrayOf(11))

        rule.launch(intent, com.jed.optima.android.support.pages.OkDialog())
            .assertText(com.jed.optima.strings.R.string.no_forms_uploaded)
    }
}
