package com.jed.optima.android.support.pages

import com.jed.optima.strings.R

class ErrorDialog : com.jed.optima.android.support.pages.OkDialog() {
    fun assertOnPage(isFatal: Boolean): ErrorDialog {
        assertOnPage()
        if (isFatal) {
            assertText(R.string.form_cannot_be_used)
        } else {
            assertText(R.string.error_occured)
        }
        return this
    }
}
