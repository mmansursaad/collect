package com.jed.optima.android.preferences.screens

import androidx.preference.Preference
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.jed.optima.fragmentstest.FragmentScenarioLauncherRule
import com.jed.optima.metadata.InstallIDProvider
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.settings.keys.ProjectKeys

@RunWith(AndroidJUnit4::class)
class FormMetadataPreferencesFragmentTest {
    private val installIDProvider = mock<InstallIDProvider>()
    private val settingsProvider =
        ApplicationProvider.getApplicationContext<com.jed.optima.android.application.Collect>().component!!.settingsProvider()

    @get:Rule
    var launcherRule = FragmentScenarioLauncherRule()

    @Before
    fun setup() {
        com.jed.optima.android.support.CollectHelpers.overrideAppDependencyModule(object : com.jed.optima.android.injection.config.AppDependencyModule() {
            override fun providesInstallIDProvider(settingsProvider: SettingsProvider): InstallIDProvider {
                return installIDProvider
            }
        })

        com.jed.optima.android.support.CollectHelpers.setupDemoProject()
    }

    @Test
    fun whenMetadataEmpty_preferenceSummariesAreNotSet() {
        whenever(installIDProvider.installID).thenReturn("")

        launcherRule
            .launch(com.jed.optima.android.preferences.screens.FormMetadataPreferencesFragment::class.java)
            .onFragment {
                assertThat(
                    it.findPreference<Preference>("metadata_username")!!.summary,
                    equalTo("Not set")
                )
                assertThat(
                    it.findPreference<Preference>("metadata_phonenumber")!!.summary,
                    equalTo("Not set")
                )
                assertThat(
                    it.findPreference<Preference>("metadata_email")!!.summary,
                    equalTo("Not set")
                )
                assertThat(
                    it.findPreference<Preference>("deviceid")!!.summary,
                    equalTo(it.context!!.getString(com.jed.optima.strings.R.string.preference_not_available))
                )
            }
    }

    @Test
    fun whenMetadataNotEmpty_preferenceSummariesAreSet() {
        whenever(installIDProvider.installID).thenReturn("123456789")
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_USERNAME, "John")
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_PHONENUMBER, "789")
        settingsProvider.getUnprotectedSettings()
            .save(ProjectKeys.KEY_METADATA_EMAIL, "john@gmail.com")

        launcherRule
            .launch(com.jed.optima.android.preferences.screens.FormMetadataPreferencesFragment::class.java)
            .onFragment {
                assertThat(
                    it.findPreference<Preference>("metadata_username")!!.summary,
                    equalTo("John")
                )
                assertThat(
                    it.findPreference<Preference>("metadata_phonenumber")!!.summary,
                    equalTo("789")
                )
                assertThat(
                    it.findPreference<Preference>("metadata_email")!!.summary,
                    equalTo("john@gmail.com")
                )
                assertThat(
                    it.findPreference<Preference>("deviceid")!!.summary,
                    equalTo("123456789")
                )
            }
    }
}
