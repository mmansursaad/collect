package com.jed.optima.android.support.pages

import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import com.jed.optima.strings.R.string
import com.jed.optima.testshared.Interactions

class FirstLaunchPage : Page<FirstLaunchPage>() {

    override fun assertOnPage(): FirstLaunchPage {
        assertText(string.configure_with_qr_code)
        return this
    }

    fun clickTryCollect(): com.jed.optima.android.support.pages.MainMenuPage {
        Interactions.clickOn(withSubstring(getTranslatedString(string.try_demo))) {
            com.jed.optima.android.support.pages.MainMenuPage().assertOnPage()
        }

        return com.jed.optima.android.support.pages.MainMenuPage()
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
