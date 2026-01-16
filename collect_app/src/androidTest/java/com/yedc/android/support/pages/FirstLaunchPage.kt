package com.yedc.android.support.pages

import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import com.yedc.strings.R.string
import com.yedc.testshared.Interactions

class FirstLaunchPage : Page<FirstLaunchPage>() {

    override fun assertOnPage(): FirstLaunchPage {
        assertText(string.configure_with_qr_code)
        return this
    }

    fun clickTryCollect(): com.yedc.android.support.pages.MainMenuPage {
        Interactions.clickOn(withSubstring(getTranslatedString(string.try_demo))) {
            com.yedc.android.support.pages.MainMenuPage().assertOnPage()
        }

        return com.yedc.android.support.pages.MainMenuPage()
    }

    fun clickManuallyEnterProjectDetails(): ManualProjectCreatorDialogPage {
        return clickOnString(
            string.configure_manually,
            ManualProjectCreatorDialogPage()
        )
    }

    fun clickConfigureWithQrCode(): QrCodeProjectCreatorDialogPage {
        return clickOnString(
            string.configure_with_qr_code,
            QrCodeProjectCreatorDialogPage()
        )
    }
}
