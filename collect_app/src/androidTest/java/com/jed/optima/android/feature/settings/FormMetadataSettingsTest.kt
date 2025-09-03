package com.jed.optima.android.feature.settings

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import com.jed.optima.android.support.pages.SaveOrDiscardFormDialog
import com.jed.optima.android.support.rules.CollectTestRule
import com.jed.optima.android.support.rules.ResetStateRule
import com.jed.optima.android.support.rules.TestRuleChain
import com.jed.optima.metadata.InstallIDProvider
import com.jed.optima.settings.SettingsProvider

@RunWith(AndroidJUnit4::class)
class FormMetadataSettingsTest {
    private val installIDProvider = FakeInstallIDProvider()
    var rule = CollectTestRule()

    @get:Rule
    var copyFormChain: RuleChain = TestRuleChain.chain()
        .around(
            ResetStateRule(
                object : com.jed.optima.android.injection.config.AppDependencyModule() {
                    override fun providesInstallIDProvider(settingsProvider: SettingsProvider): InstallIDProvider {
                        return installIDProvider
                    }
                }
            )
        )
        .around(rule)

    @Test
    fun metadataShouldBeDisplayedInPreferences() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickUserAndDeviceIdentity()
            .clickFormMetadata()

            .clickUsername()
            .inputText("Chino")
            .clickOKOnDialog()
            .clickPhoneNumber()
            .inputText("123")
            .clickOKOnDialog()
            .clickEmail()
            .inputText("chino@whitepony.com")
            .clickOKOnDialog()

            .assertPreference(com.jed.optima.strings.R.string.username, "Chino")
            .assertPreference(com.jed.optima.strings.R.string.phone_number, "123")
            .assertPreference(com.jed.optima.strings.R.string.email, "chino@whitepony.com")
            .assertPreference(com.jed.optima.strings.R.string.device_id, installIDProvider.installID)
    }

    @Test
    fun metadataShouldBeDisplayedInForm() {
        rule.startAtMainMenu()
            .copyForm("metadata.xml")

            .openProjectSettingsDialog()
            .clickSettings()
            .clickUserAndDeviceIdentity()
            .clickFormMetadata()
            .clickUsername()
            .inputText("Chino")
            .clickOKOnDialog()
            .clickPhoneNumber()
            .inputText("664615")
            .clickOKOnDialog()
            .clickEmail()
            .inputText("chino@whitepony.com")
            .clickOKOnDialog()
            .pressBack(com.jed.optima.android.support.pages.UserAndDeviceIdentitySettingsPage())
            .pressBack(com.jed.optima.android.support.pages.ProjectSettingsPage())
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())

            // And verify that new metadata is displayed
            .startBlankForm("Metadata")
            .assertTexts("Chino", "664615", "chino@whitepony.com", installIDProvider.installID)
    }

    @Test
    fun settingServerUsername_usedAsFallbackForMetadataUsername() {
        rule.startAtMainMenu()
            .copyForm("metadata.xml")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickServerSettings()
            .clickServerUsername()
            .inputText("Chino")
            .clickOKOnDialog()
            .pressBack(com.jed.optima.android.support.pages.ProjectSettingsPage())
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())
            .startBlankForm("Metadata")
            .assertText("Chino")
            .pressBack(SaveOrDiscardFormDialog(com.jed.optima.android.support.pages.MainMenuPage()))
            .clickDiscardForm()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickUserAndDeviceIdentity()
            .clickFormMetadata()
            .clickUsername()
            .inputText("Stephen")
            .clickOKOnDialog()
            .pressBack(com.jed.optima.android.support.pages.UserAndDeviceIdentitySettingsPage())
            .pressBack(com.jed.optima.android.support.pages.ProjectSettingsPage())
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())
            .startBlankForm("Metadata")
            .assertText("Stephen")
    }

    @Test // https://github.com/getodk/collect/issues/4792
    fun metadataProperties_shouldBeReloadedAfterSwitchingProjects() {
        rule.startAtMainMenu()
            .copyForm("metadata.xml")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickUserAndDeviceIdentity()
            .clickFormMetadata()
            .clickEmail()
            .inputText("demo@getodk.com")
            .clickOKOnDialog()
            .clickPhoneNumber()
            .inputText("123456789")
            .clickOKOnDialog()
            .clickUsername()
            .inputText("Demo user")
            .clickOKOnDialog()
            .pressBack(com.jed.optima.android.support.pages.UserAndDeviceIdentitySettingsPage())
            .pressBack(com.jed.optima.android.support.pages.ProjectSettingsPage())
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())

            .addAndSwitchToProject("https://second-project.com")
            .copyForm("metadata.xml", "second-project.com")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickUserAndDeviceIdentity()
            .clickFormMetadata()
            .clickEmail()
            .inputText("john@second-project.com")
            .clickOKOnDialog()
            .clickPhoneNumber()
            .inputText("987654321")
            .clickOKOnDialog()
            .clickUsername()
            .inputText("John Smith")
            .clickOKOnDialog()
            .pressBack(com.jed.optima.android.support.pages.UserAndDeviceIdentitySettingsPage())
            .pressBack(com.jed.optima.android.support.pages.ProjectSettingsPage())
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())

            .clickFillBlankForm()
            .clickOnForm("Metadata")
            .assertTexts("john@second-project.com", "987654321", "John Smith")
            .swipeToEndScreen()
            .clickFinalize()

            .openProjectSettingsDialog()
            .selectProject("Demo project")
            .clickFillBlankForm()
            .clickOnForm("Metadata")
            .assertTexts("demo@getodk.com", "123456789", "Demo user")
            .swipeToEndScreen()
            .clickFinalize()
    }

    @Test
    fun setEmail_validatesEmail() {
        rule.startAtMainMenu()
            .copyForm("metadata.xml")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickUserAndDeviceIdentity()
            .clickFormMetadata()
            .clickEmail()
            .inputText("aabb")
            .clickOKOnDialog()
            .checkIsToastWithMessageDisplayed(com.jed.optima.strings.R.string.invalid_email_address)
            .clickEmail()
            .inputText("aa@bb")
            .clickOKOnDialog()
            .assertText("aa@bb")
    }

    private class FakeInstallIDProvider : InstallIDProvider {
        override val installID: String
            get() = "deviceID"
    }
}
