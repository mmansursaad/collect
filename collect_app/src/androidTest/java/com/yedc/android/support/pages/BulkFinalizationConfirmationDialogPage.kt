package com.yedc.android.support.pages

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.yedc.strings.R
import com.yedc.strings.R.plurals
import com.yedc.strings.localization.getLocalizedQuantityString

class BulkFinalizationConfirmationDialogPage(private val count: Int) : Page<BulkFinalizationConfirmationDialogPage>() {
    override fun assertOnPage(): BulkFinalizationConfirmationDialogPage {
        val title = ApplicationProvider.getApplicationContext<Application>()
            .getLocalizedQuantityString(plurals.bulk_finalize_confirmation, count, count)

        onView(withText(title)).inRoot(isDialog()).check(matches(isDisplayed()))
        return this
    }

    fun clickFinalize(): com.yedc.android.support.pages.EditSavedFormPage {
        return this.clickOnTextInDialog(R.string.finalize, AsyncPage(com.yedc.android.support.pages.EditSavedFormPage()))
    }

    fun clickCancel(): com.yedc.android.support.pages.EditSavedFormPage {
        return this.clickOnTextInDialog(R.string.cancel,
            com.yedc.android.support.pages.EditSavedFormPage()
        )
    }
}
