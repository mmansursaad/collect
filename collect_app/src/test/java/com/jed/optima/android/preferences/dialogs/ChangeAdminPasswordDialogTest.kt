package com.jed.optima.android.preferences.dialogs

import android.content.Context
import android.text.InputType
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import com.jed.optima.android.preferences.ProjectPreferencesViewModel
import com.jed.optima.fragmentstest.FragmentScenarioLauncherRule
import com.jed.optima.settings.InMemSettingsProvider
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.settings.keys.ProtectedProjectKeys
import com.jed.optima.testshared.RobolectricHelpers
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class ChangeAdminPasswordDialogTest {

    private val settingsProvider = InMemSettingsProvider()
    private val projectPreferencesViewModel = mock<ProjectPreferencesViewModel>()

    @Inject
    lateinit var factory: ProjectPreferencesViewModel.Factory

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    @Before
    fun setup() {
        com.jed.optima.android.support.CollectHelpers.overrideAppDependencyModule(object : com.jed.optima.android.injection.config.AppDependencyModule() {
            override fun providesSettingsProvider(context: Context?): SettingsProvider {
                return settingsProvider
            }

            override fun providesProjectPreferencesViewModel(adminPasswordProvider: com.jed.optima.android.utilities.AdminPasswordProvider): ProjectPreferencesViewModel.Factory {
                return object : ProjectPreferencesViewModel.Factory(adminPasswordProvider) {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return projectPreferencesViewModel as T
                    }
                }
            }
        })
    }

    @Test
    fun `The dialog should be dismissed after clicking on a device back button`() {
        val scenario = launcherRule.launch(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, `is`(true))
            Espresso.onView(ViewMatchers.isRoot()).perform(ViewActions.pressBack())
            assertThat(it.dialog, `is`(Matchers.nullValue()))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on 'OK'`() {
        val scenario = launcherRule.launch(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, `is`(true))
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            RobolectricHelpers.runLooper()
            assertThat(it.dialog, `is`(Matchers.nullValue()))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on 'CANCEL'`() {
        val scenario = launcherRule.launch(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, Matchers.`is`(true))
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
            RobolectricHelpers.runLooper()
            assertThat(it.dialog, `is`(Matchers.nullValue()))
        }
    }

    @Test
    fun `Setting password and accepting updates the password in settings`() {
        val scenario = launcherRule.launch(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_ADMIN_PW, "")
            it.binding.pwdField.setText("password")
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            RobolectricHelpers.runLooper()
            assertThat(settingsProvider.getProtectedSettings().getString(ProtectedProjectKeys.KEY_ADMIN_PW), `is`("password"))
        }
    }

    @Test
    fun `Setting password and canceling does not update the password in settings`() {
        val scenario = launcherRule.launch(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_ADMIN_PW, "")
            it.binding.pwdField.setText("password")
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
            RobolectricHelpers.runLooper()
            assertThat(settingsProvider.getProtectedSettings().getString(ProtectedProjectKeys.KEY_ADMIN_PW), `is`(""))
        }
    }

    @Test
    fun `Setting password sets Unlocked state in view model`() {
        val scenario = launcherRule.launch(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            it.binding.pwdField.setText("password")
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            RobolectricHelpers.runLooper()
            verify(projectPreferencesViewModel).setStateUnlocked()
            verifyNoMoreInteractions(projectPreferencesViewModel)
        }
    }

    @Test
    fun `Removing password sets NotProtected state in view model`() {
        val scenario = launcherRule.launch(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            it.binding.pwdField.setText("")
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            RobolectricHelpers.runLooper()
            verify(projectPreferencesViewModel).setStateNotProtected()
            verifyNoMoreInteractions(projectPreferencesViewModel)
        }
    }

    @Test
    fun `When screen is rotated password and checkbox value is retained`() {
        val scenario = launcherRule.launch(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            it.binding.pwdField.setText("password")
            it.binding.checkBox2.performClick()
            scenario.recreate()
            assertThat(it.binding.pwdField.text.toString(), `is`("password"))
            assertThat(it.binding.checkBox2.isChecked, `is`(true))
        }
    }

    @Test
    fun `'Show password' displays and hides password`() {
        val scenario = launcherRule.launch(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            it.binding.checkBox2.performClick()
            assertThat(it.binding.pwdField.inputType, `is`(InputType.TYPE_TEXT_VARIATION_PASSWORD))
            it.binding.checkBox2.performClick()
            assertThat(it.binding.pwdField.inputType, `is`(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD))
        }
    }
}
