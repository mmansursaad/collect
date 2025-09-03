package com.jed.optima.android.support.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText

class QrCodeProjectCreatorDialogPage : Page<QrCodeProjectCreatorDialogPage>() {
    override fun assertOnPage(): QrCodeProjectCreatorDialogPage {
        assertText(com.jed.optima.strings.R.string.add_project)
        return this
    }

    fun switchToManualMode(): ManualProjectCreatorDialogPage {
        return clickOnTextInDialog(com.jed.optima.strings.R.string.configure_manually, ManualProjectCreatorDialogPage())
    }

    fun assertDuplicateDialogShown(): QrCodeProjectCreatorDialogPage {
        onView(withText(getTranslatedString(com.jed.optima.strings.R.string.duplicate_project_details)))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        return this
    }

    fun switchToExistingProject(): com.jed.optima.android.support.pages.MainMenuPage {
        return clickOnTextInDialog(com.jed.optima.strings.R.string.switch_to_existing,
            com.jed.optima.android.support.pages.MainMenuPage()
        )
    }

    fun addDuplicateProject(): com.jed.optima.android.support.pages.MainMenuPage {
        return clickOnTextInDialog(com.jed.optima.strings.R.string.add_duplicate_project,
            com.jed.optima.android.support.pages.MainMenuPage()
        )
    }
}
