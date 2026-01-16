package com.yedc.android.activities

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import com.yedc.android.R
import com.yedc.android.projects.ManualProjectCreatorDialog
import com.yedc.android.projects.QrCodeProjectCreatorDialog
import com.yedc.androidtest.ActivityScenarioLauncherRule
import com.yedc.androidtest.RecordedIntentsRule
import com.yedc.qrcode.BarcodeScannerViewContainer
import com.yedc.settings.SettingsProvider
import com.yedc.strings.localization.getLocalizedString
import com.yedc.testshared.FakeBarcodeScannerViewFactory
import com.yedc.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class FirstLaunchActivityTest {

    @get:Rule
    val launcherRule = ActivityScenarioLauncherRule()

    @get:Rule
    val activityRule = RecordedIntentsRule()

    @Before
    fun setup() {
        com.yedc.android.support.CollectHelpers.overrideAppDependencyModule(object : _root_ide_package_.com.yedc.android.injection.config.AppDependencyModule() {
            override fun providesBarcodeScannerViewFactory(settingsProvider: SettingsProvider): BarcodeScannerViewContainer.Factory {
                return FakeBarcodeScannerViewFactory()
            }
        })
    }

    @Test
    fun `The QrCodeProjectCreatorDialog should be displayed after clicking on the 'Configure with QR code' button`() {
        val scenario = launcherRule.launch(FirstLaunchActivity::class.java)
        scenario.onActivity {
            onView(withText(com.yedc.strings.R.string.configure_with_qr_code)).perform(click())
            assertThat(
                it.supportFragmentManager.findFragmentByTag(QrCodeProjectCreatorDialog::class.java.name),
                `is`(notNullValue())
            )
        }
    }

    @Test
    fun `The ManualProjectCreatorDialog should be displayed after clicking on the 'Configure manually' button`() {
        val scenario = launcherRule.launch(FirstLaunchActivity::class.java)
        scenario.onActivity {
            onView(withText(com.yedc.strings.R.string.configure_manually)).perform(click())
            assertThat(
                it.supportFragmentManager.findFragmentByTag(ManualProjectCreatorDialog::class.java.name),
                `is`(notNullValue())
            )
        }
    }

    @Test
    fun `The ODK logo should be displayed`() {
        val scenario = launcherRule.launch(FirstLaunchActivity::class.java)
        scenario.onActivity {
            onView(withId(R.id.logo)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun `The app name with its version should be displayed`() {
        val versionInformation = mock(_root_ide_package_.com.yedc.android.version.VersionInformation::class.java)
        whenever(versionInformation.versionToDisplay).thenReturn("vfake")
        com.yedc.android.support.CollectHelpers.overrideAppDependencyModule(object : _root_ide_package_.com.yedc.android.injection.config.AppDependencyModule() {
            override fun providesVersionInformation(): _root_ide_package_.com.yedc.android.version.VersionInformation {
                return versionInformation
            }
        })

        val scenario = launcherRule.launch(FirstLaunchActivity::class.java)
        scenario.onActivity {
            verify(versionInformation).versionToDisplay
            onView(
                withText(
                    ApplicationProvider.getApplicationContext<_root_ide_package_.com.yedc.android.application.Collect>().getLocalizedString(
                        com.yedc.strings.R.string.collect_app_name
                    ) + " vfake"
                )
            ).perform(scrollTo()).check(matches(isDisplayed()))
        }
    }

    @Test
    fun `Adding demo project displays a progress dialog`() {
        val scenario = launcherRule.launch(FirstLaunchActivity::class.java)
        scenario.onActivity {
            val dialogClass = com.yedc.material.MaterialProgressDialogFragment::class.java
            assertThat(RobolectricHelpers.getFragmentByClass(it.supportFragmentManager, dialogClass), nullValue())

            onView(
                withText(
                    containsString(
                        ApplicationProvider.getApplicationContext<_root_ide_package_.com.yedc.android.application.Collect>().getLocalizedString(
                            com.yedc.strings.R.string.try_demo
                        )
                    )
                )
            ).perform(scrollTo()).perform(click())

            assertThat(RobolectricHelpers.getFragmentByClass(it.supportFragmentManager, dialogClass), notNullValue())
        }
    }
}
