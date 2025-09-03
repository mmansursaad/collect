package com.jed.optima.android.preferences.screens

import androidx.lifecycle.ViewModel
import androidx.preference.Preference
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import com.jed.optima.android.TestSettingsProvider
import com.jed.optima.android.application.FeatureFlags
import com.jed.optima.android.preferences.ProjectPreferencesViewModel
import com.jed.optima.fragmentstest.FragmentScenarioLauncherRule
import com.jed.optima.settings.keys.ProjectKeys
import com.jed.optima.settings.keys.ProtectedProjectKeys
import com.jed.optima.shared.settings.Settings

@RunWith(AndroidJUnit4::class)
class UserInterfacePreferencesFragmentTest {
    private lateinit var generalSettings: Settings
    private lateinit var adminSettings: Settings

    private val adminPasswordProvider = mock<com.jed.optima.android.utilities.AdminPasswordProvider> {
        on { isAdminPasswordSet } doReturn false
    }
    private val projectPreferencesViewModel = ProjectPreferencesViewModel(adminPasswordProvider)

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    @Before
    fun setup() {
        com.jed.optima.android.support.CollectHelpers.overrideAppDependencyModule(object : com.jed.optima.android.injection.config.AppDependencyModule() {
            override fun providesProjectPreferencesViewModel(adminPasswordProvider: com.jed.optima.android.utilities.AdminPasswordProvider): ProjectPreferencesViewModel.Factory {
                return object : ProjectPreferencesViewModel.Factory(adminPasswordProvider) {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return projectPreferencesViewModel as T
                    }
                }
            }
        })

        com.jed.optima.android.support.CollectHelpers.setupDemoProject()
        generalSettings = TestSettingsProvider.getUnprotectedSettings()
        adminSettings = TestSettingsProvider.getProtectedSettings()
    }

    @Test
    fun `Enabled preferences should be visible in Locked mode`() {
        projectPreferencesViewModel.setStateLocked()

        val scenario = launcherRule.launch(com.jed.optima.android.preferences.screens.UserInterfacePreferencesFragment::class.java)
        scenario.onFragment { fragment: com.jed.optima.android.preferences.screens.UserInterfacePreferencesFragment ->
            if (!FeatureFlags.NO_THEME_SETTING) {
                assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_THEME)!!.isVisible, `is`(true))
            }

            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_LANGUAGE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_FONT_SIZE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_NAVIGATION)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `Disabled preferences should be hidden in Locked mode`() {
        adminSettings.save(ProtectedProjectKeys.KEY_APP_THEME, false)
        adminSettings.save(ProtectedProjectKeys.KEY_APP_LANGUAGE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_CHANGE_FONT_SIZE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_NAVIGATION, false)

        projectPreferencesViewModel.setStateLocked()

        val scenario = launcherRule.launch(com.jed.optima.android.preferences.screens.UserInterfacePreferencesFragment::class.java)
        scenario.onFragment { fragment: com.jed.optima.android.preferences.screens.UserInterfacePreferencesFragment ->
            if (!FeatureFlags.NO_THEME_SETTING) {
                assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_THEME)!!.isVisible, `is`(false))
            }

            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_LANGUAGE)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_FONT_SIZE)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_NAVIGATION)!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `Enabled preferences should be visible in Unlocked mode`() {
        projectPreferencesViewModel.setStateUnlocked()

        val scenario = launcherRule.launch(com.jed.optima.android.preferences.screens.UserInterfacePreferencesFragment::class.java)
        scenario.onFragment { fragment: com.jed.optima.android.preferences.screens.UserInterfacePreferencesFragment ->
            if (!FeatureFlags.NO_THEME_SETTING) {
                assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_THEME)!!.isVisible, `is`(true))
            }

            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_LANGUAGE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_FONT_SIZE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_NAVIGATION)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `Disabled preferences should be visible in Unlocked mode`() {
        adminSettings.save(ProtectedProjectKeys.KEY_APP_THEME, false)
        adminSettings.save(ProtectedProjectKeys.KEY_APP_LANGUAGE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_CHANGE_FONT_SIZE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_NAVIGATION, false)

        projectPreferencesViewModel.setStateUnlocked()

        val scenario = launcherRule.launch(com.jed.optima.android.preferences.screens.UserInterfacePreferencesFragment::class.java)
        scenario.onFragment { fragment: com.jed.optima.android.preferences.screens.UserInterfacePreferencesFragment ->
            if (!FeatureFlags.NO_THEME_SETTING) {
                assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_THEME)!!.isVisible, `is`(true))
            }

            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_LANGUAGE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_FONT_SIZE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_NAVIGATION)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `Enabled preferences should be visible in NotProtected mode`() {
        projectPreferencesViewModel.setStateNotProtected()

        val scenario = launcherRule.launch(com.jed.optima.android.preferences.screens.UserInterfacePreferencesFragment::class.java)
        scenario.onFragment { fragment: com.jed.optima.android.preferences.screens.UserInterfacePreferencesFragment ->
            if (!FeatureFlags.NO_THEME_SETTING) {
                assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_THEME)!!.isVisible, `is`(true))
            }

            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_LANGUAGE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_FONT_SIZE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_NAVIGATION)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `Disabled preferences should be hidden in NotProtected mode`() {
        adminSettings.save(ProtectedProjectKeys.KEY_APP_THEME, false)
        adminSettings.save(ProtectedProjectKeys.KEY_APP_LANGUAGE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_CHANGE_FONT_SIZE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_NAVIGATION, false)

        projectPreferencesViewModel.setStateNotProtected()

        val scenario = launcherRule.launch(com.jed.optima.android.preferences.screens.UserInterfacePreferencesFragment::class.java)
        scenario.onFragment { fragment: com.jed.optima.android.preferences.screens.UserInterfacePreferencesFragment ->
            if (!FeatureFlags.NO_THEME_SETTING) {
                assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_THEME)!!.isVisible, `is`(false))
            }

            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_LANGUAGE)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_FONT_SIZE)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_NAVIGATION)!!.isVisible, `is`(false))
        }
    }
}
