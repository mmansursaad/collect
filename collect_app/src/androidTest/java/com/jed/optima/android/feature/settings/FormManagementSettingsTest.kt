package com.jed.optima.android.feature.settings

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import com.jed.optima.android.support.TestDependencies
import com.jed.optima.android.support.rules.CollectTestRule
import com.jed.optima.android.support.rules.TestRuleChain
import com.jed.optima.androidtest.RecordedIntentsRule

@RunWith(AndroidJUnit4::class)
class FormManagementSettingsTest {
    private val testDependencies = TestDependencies()
    private val rule = CollectTestRule()

    @get:Rule
    var ruleChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(RecordedIntentsRule())
        .around(rule)

    @Test
    fun whenMatchExactlyEnabled_changingAutomaticUpdateFrequency_changesTaskFrequency() {
        var deferredTasks = testDependencies.scheduler.getDeferredTasks()

        assertThat(deferredTasks, `is`(empty()))

        val page = com.jed.optima.android.support.pages.MainMenuPage().assertOnPage()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickFormManagement()
            .clickUpdateForms()
            .clickOption(com.jed.optima.strings.R.string.match_exactly)

        deferredTasks = testDependencies.scheduler.getDeferredTasks()

        assertThat(deferredTasks.size, `is`(1))

        val matchExactlyTag = deferredTasks[0].tag

        page.clickAutomaticUpdateFrequency().clickOption(com.jed.optima.strings.R.string.every_one_hour)
        deferredTasks = testDependencies.scheduler.getDeferredTasks()

        assertThat(deferredTasks.size, `is`(1))
        assertThat(deferredTasks[0].tag, `is`(matchExactlyTag))
        assertThat(deferredTasks[0].repeatPeriod, `is`(1000L * 60 * 60))
    }

    @Test
    fun whenPreviouslyDownloadedOnlyEnabled_changingAutomaticUpdateFrequency_changesTaskFrequency() {
        var deferredTasks = testDependencies.scheduler.getDeferredTasks()

        assertThat(deferredTasks, `is`(empty()))

        val page = com.jed.optima.android.support.pages.MainMenuPage().assertOnPage()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickFormManagement()
            .clickUpdateForms()
            .clickOption(com.jed.optima.strings.R.string.previously_downloaded_only)

        deferredTasks = testDependencies.scheduler.getDeferredTasks()

        assertThat(deferredTasks.size, `is`(1))

        val previouslyDownloadedTag = deferredTasks[0].tag
        page.clickAutomaticUpdateFrequency().clickOption(com.jed.optima.strings.R.string.every_one_hour)

        deferredTasks = testDependencies.scheduler.getDeferredTasks()

        assertThat(deferredTasks.size, `is`(1))
        assertThat(deferredTasks[0].tag, `is`(previouslyDownloadedTag))
        assertThat(deferredTasks[0].repeatPeriod, `is`(1000L * 60 * 60))
    }
}
