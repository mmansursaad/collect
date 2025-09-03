package com.jed.optima.android.feature.settings

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import com.jed.optima.android.application.FeatureFlags
import com.jed.optima.android.support.rules.CollectTestRule
import com.jed.optima.android.support.rules.TestRuleChain
import com.jed.optima.strings.R

@RunWith(AndroidJUnit4::class)
class FormEntrySettingsTest {

    private val rule = CollectTestRule()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain().around(rule)

    @Test
    fun settingsThatResetAppAreBlocked() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .clickOptionsIcon()
            .clickProjectSettings()
            .assertDisabled(R.string.project_management_section_title)

            .clickOnUserInterface()
            .assertDisabled(R.string.language)
            .also {
                if (!FeatureFlags.NO_THEME_SETTING) {
                    it.assertDisabled(R.string.app_theme)
                }
            }
    }
}
