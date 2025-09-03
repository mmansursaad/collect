package com.jed.optima.android.support.pages

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.jed.optima.strings.R
import com.jed.optima.strings.R.plurals
import com.jed.optima.strings.localization.getLocalizedQuantityString

class BulkFinalizationConfirmationDialogPage(private val count: Int) : Page<BulkFinalizationConfirmationDialogPage>() {
    override fun assertOnPage(): BulkFinalizationConfirmationDialogPage {
        val title = ApplicationProvider.getApplicationContext<Application>()
            .getLocalizedQuantityString(plurals.bulk_finalize_confirmation, count, count)

        onView(withText(title)).inRoot(isDialog()).check(matches(isDisplayed()))
        return this
    }

    fun clickFinalize(): com.jed.optima.android.support.pages.EditSavedFormPage {
        return this.clickOnTextInDialog(R.string.finalize, AsyncPage(com.jed.optima.android.support.pages.EditSavedFormPage()))
    }

    fun clickCancel(): com.jed.optima.android.support.pages.EditSavedFormPage {
        return this.clickOnTextInDialog(R.string.cancel,
            com.jed.optima.android.support.pages.EditSavedFormPage()
        )
    }
}
