package com.jed.optima.android.feature.projects

import android.app.Application
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import com.jed.optima.android.support.rules.CollectTestRule
import com.jed.optima.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class ProjectsAdbTest {

    val rule = CollectTestRule()

    @get:Rule
    var chain: RuleChain = TestRuleChain
        .chain()
        .around(rule)

    @Test
    fun clearingStorage_andReturningToApp_recreatesStorageForProject() {
        val fillBlankFormPage = rule.startAtMainMenu().clickFillBlankForm()

        val storage = getApplicationContext<Application>().getExternalFilesDir(null)
        storage!!.listFiles()!!.forEach { it.deleteRecursively() }

        fillBlankFormPage.pressBack(com.jed.optima.android.support.pages.MainMenuPage())
            .assertProjectIcon("D")
    }
}
