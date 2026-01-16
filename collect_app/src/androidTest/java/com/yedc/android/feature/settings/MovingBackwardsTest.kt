package com.yedc.android.feature.settings

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import com.yedc.android.support.pages.AccessControlPage
import com.yedc.android.support.rules.CollectTestRule
import com.yedc.android.support.rules.TestRuleChain

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
            .clickOnString(com.yedc.strings.R.string.yes)

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
            .pressBack(com.yedc.android.support.pages.ProjectSettingsPage())
            .openFormManagement()
            .scrollToConstraintProcessing()
            .checkIfConstraintProcessingIsDisabled()
            .assertTextDoesNotExist(com.yedc.strings.R.string.constraint_behavior_on_finalize)
            .assertText(com.yedc.strings.R.string.constraint_behavior_on_swipe)
    }

    @Test
    fun whenMovingBackwardDisabledWithoutPreventingUsersFormBypassingIt_relatedOptionsShouldNotBeUpdated() {
        rule.startAtMainMenu()
            .disableFinalizeInFormEntry()
            .setConstraintProcessingToOnFinalize()
            .assertSettingsBeforeDisablingMovingBackwards()

            .clickMovingBackwards()
            .clickOnString(com.yedc.strings.R.string.no)

            .assertGoToPromptEnabled()
            .assertGoToPromptChecked()

            .assertSaveAsDraftInFormEntryEnabled()
            .assertSaveAsDraftInFormEntryChecked()

            .assertSaveAsDraftInFormEndDisabled()
            .assertSaveAsDraftInFormEndChecked()

            .assertFinalizeEnabled()
            .assertFinalizeUnchecked()

            .pressBack(AccessControlPage())
            .pressBack(com.yedc.android.support.pages.ProjectSettingsPage())
            .openFormManagement()
            .scrollToConstraintProcessing()
            .checkIfConstraintProcessingIsEnabled()
            .assertText(com.yedc.strings.R.string.constraint_behavior_on_finalize)
            .assertTextDoesNotExist(com.yedc.strings.R.string.constraint_behavior_on_swipe)
    }

    private fun com.yedc.android.support.pages.MainMenuPage.disableFinalizeInFormEntry(): com.yedc.android.support.pages.ProjectSettingsPage {
        return openProjectSettingsDialog()
            .clickSettings()
            .clickAccessControl()
            .clickFormEntrySettings()
            .clickOnString(com.yedc.strings.R.string.finalize)
            .pressBack(AccessControlPage())
            .pressBack(com.yedc.android.support.pages.ProjectSettingsPage())
    }

    private fun com.yedc.android.support.pages.ProjectSettingsPage.setConstraintProcessingToOnFinalize(): com.yedc.android.support.pages.ProjectSettingsPage {
        return openFormManagement()
            .openConstraintProcessing()
            .clickOnString(com.yedc.strings.R.string.constraint_behavior_on_finalize)
            .pressBack(com.yedc.android.support.pages.ProjectSettingsPage())
    }

    private fun com.yedc.android.support.pages.ProjectSettingsPage.assertSettingsBeforeDisablingMovingBackwards(): AccessControlPage {
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
