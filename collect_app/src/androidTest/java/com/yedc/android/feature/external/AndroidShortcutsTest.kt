package com.yedc.android.feature.external

import android.content.Intent
import android.content.Intent.EXTRA_SHORTCUT_INTENT
import android.content.Intent.EXTRA_SHORTCUT_NAME
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import com.yedc.android.support.ContentProviderUtils
import com.yedc.android.support.TestDependencies
import com.yedc.android.support.rules.CollectTestRule
import com.yedc.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class AndroidShortcutsTest {

    private val rule = CollectTestRule()
    private val testDependencies = TestDependencies()

    @get:Rule
    var ruleChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(rule)

    @Test
    fun showsFormsForCurrentProject_andUpdatesListWhenNewFormsAreDownloaded() {
        testDependencies.server.addForm(
            "One Question",
            "one_question",
            "1",
            "one-question.xml"
        )

        rule.startAtMainMenu()
            .setServer(testDependencies.server.url)
            .enableMatchExactly()

        val shortcutsPage = rule.launchShortcuts()
            .assertTextDoesNotExist("One Question")

        testDependencies.scheduler.runDeferredTasks()

        shortcutsPage.asyncAssertText("One Question")
    }

    @Test
    fun shortcutIsFormEditAction() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .clickFillBlankForm() // Load form

        val shortcutIntent = rule.launchShortcuts()
            .selectForm("One Question")
        assertThat(shortcutIntent.getStringExtra(EXTRA_SHORTCUT_NAME), equalTo("One Question"))

        val shortcutTargetIntent =
            shortcutIntent.getParcelableExtra<Intent>(EXTRA_SHORTCUT_INTENT)!!
        val formId = ContentProviderUtils.getFormDatabaseId("DEMO", "one_question")
        assertThat(shortcutTargetIntent.action, equalTo(Intent.ACTION_EDIT))
        assertThat(shortcutTargetIntent.data, equalTo(_root_ide_package_.com.yedc.android.external.FormsContract.getUri("DEMO", formId)))
    }
}
