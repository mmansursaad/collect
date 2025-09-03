package com.jed.optima.android.preferences.dialogs

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.jed.optima.fragmentstest.FragmentScenarioLauncherRule
import com.jed.optima.permissions.R
import com.jed.optima.strings.localization.getLocalizedString

@RunWith(AndroidJUnit4::class)
class ResetProgressDialogTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    @Test
    fun `The dialog should not be dismissed after clicking out of its area or on device back button`() {
        val scenario = launcherRule.launch(ResetProgressDialog::class.java)
        scenario.onFragment {
            assertThat(it.isCancelable, `is`(false))
        }
    }

    @Test
    fun `The dialog should display proper content`() {
        val scenario = launcherRule.launch(ResetProgressDialog::class.java)
        scenario.onFragment {
            // Title
            assertThat(it.title, `is`(context.getLocalizedString(com.jed.optima.strings.R.string.please_wait)))

            // Message
            assertThat(it.message, `is`(context.getLocalizedString(com.jed.optima.strings.R.string.reset_in_progress)))
        }
    }
}
