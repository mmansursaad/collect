package com.jed.optima.android.feature.settings

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import com.jed.optima.android.support.pages.AccessControlPage
import com.jed.optima.android.support.rules.CollectTestRule
import com.jed.optima.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class MovingBackwardsTest {
    private val rule = CollectTestRule()

    @get:Rule
    var ruleChain: RuleChain = TestRuleChain.chain().around(rule)

    @Test
    fun whenMovingBackwardDisabledWithPreventingUsersFormBypassingIt_relatedOptionsShouldBeUpdated() {
        rule.startAtMainMenu()
            .disableFinalizeInFormEntry()
            .setConstraintProcessingToOnFinalize()
            .assertSettingsBeforeDisablingMovingBackwards()

            .clickMovingBackwards()
            .clickOnString(com.jed.optima.strings.R.string.yes)

            // after disabling moving backward - the state of the 4 related options is reversed
            .assertGoToPromptDisabled()
            .assertGoToPromptUnchecked()

            .assertSaveAsDraftInFormEntryDisabled()
            .assertSaveAsDraftInFormEntryUnchecked()

            .assertSaveAsDraftInFormEndDisabled()
            .assertSaveAsDraftInFormEndUnchecked()

            .assertFinalizeDisabled()
            .assertFinalizeChecked()

            .pressBack(AccessControlPage())
            .pressBack(com.jed.optima.android.support.pages.ProjectSettingsPage())
            .openFormManagement()
            .scrollToConstraintProcessing()
            .checkIfConstraintProcessingIsDisabled()
            .assertTextDoesNotExist(com.jed.optima.strings.R.string.constraint_behavior_on_finalize)
            .assertText(com.jed.optima.strings.R.string.constraint_behavior_on_swipe)
    }

    @Test
    fun whenMovingBackwardDisabledWithoutPreventingUsersFormBypassingIt_relatedOptionsShouldNotBeUpdated() {
        rule.startAtMainMenu()
            .disableFinalizeInFormEntry()
            .setConstraintProcessingToOnFinalize()
            .assertSettingsBeforeDisablingMovingBackwards()

            .clickMovingBackwards()
            .clickOnString(com.jed.optima.strings.R.string.no)

            .assertGoToPromptEnabled()
            .assertGoToPromptChecked()

            .assertSaveAsDraftInFormEntryEnabled()
            .assertSaveAsDraftInFormEntryChecked()

            .assertSaveAsDraftInFormEndDisabled()
            .assertSaveAsDraftInFormEndChecked()

            .assertFinalizeEnabled()
            .assertFinalizeUnchecked()

            .pressBack(AccessControlPage())
            .pressBack(com.jed.optima.android.support.pages.ProjectSettingsPage())
            .openFormManagement()
            .scrollToConstraintProcessing()
            .checkIfConstraintProcessingIsEnabled()
            .assertText(com.jed.optima.strings.R.string.constraint_behavior_on_finalize)
            .assertTextDoesNotExist(com.jed.optima.strings.R.string.constraint_behavior_on_swipe)
    }

    private fun com.jed.optima.android.support.pages.MainMenuPage.disableFinalizeInFormEntry(): com.jed.optima.android.support.pages.ProjectSettingsPage {
        return openProjectSettingsDialog()
            .clickSettings()
            .clickAccessControl()
            .clickFormEntrySettings()
            .clickOnString(com.jed.optima.strings.R.string.finalize)
            .pressBack(AccessControlPage())
            .pressBack(com.jed.optima.android.support.pages.ProjectSettingsPage())
    }

    private fun com.jed.optima.android.support.pages.ProjectSettingsPage.setConstraintProcessingToOnFinalize(): com.jed.optima.android.support.pages.ProjectSettingsPage {
        return openFormManagement()
            .openConstraintProcessing()
            .clickOnString(com.jed.optima.strings.R.string.constraint_behavior_on_finalize)
            .pressBack(com.jed.optima.android.support.pages.ProjectSettingsPage())
    }

    private fun com.jed.optima.android.support.pages.ProjectSettingsPage.assertSettingsBeforeDisablingMovingBackwards(): AccessControlPage {
        return clickAccessControl()
            .clickFormEntrySettings()
            .assertGoToPromptEnabled()
            .assertGoToPromptChecked()

            .assertSaveAsDraftInFormEntryEnabled()
            .assertSaveAsDraftInFormEntryChecked()

            .assertSaveAsDraftInFormEndDisabled()
            .assertSaveAsDraftInFormEndChecked()

            .assertFinalizeEnabled()
            .assertFinalizeUnchecked()
    }
}
