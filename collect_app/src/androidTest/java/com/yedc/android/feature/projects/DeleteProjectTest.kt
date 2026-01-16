package com.yedc.android.feature.projects

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import com.yedc.android.R
import com.yedc.android.support.rules.CollectTestRule
import com.yedc.android.support.rules.TestRuleChain

class DeleteProjectTest {

    val rule = CollectTestRule()

    @get:Rule
    var chain: RuleChain = TestRuleChain
        .chain()
        .around(rule)

    @Test
    fun deleteProjectTest() {
        // Add project Turtle nesting
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()
            .switchToManualMode()
            .inputUrl("https://my-server.com")
            .inputUsername("John")
            .addProject()

            // Delete Turtle nesting project
            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .deleteProject()

            // Assert switching to Turtle nesting
            .checkIsToastWithMessageDisplayed(com.yedc.strings.R.string.switched_project, "Demo project")
            .assertProjectIcon("D")

            // Delete Demo project
            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectManagement()
            .deleteLastProject()
    }
}
