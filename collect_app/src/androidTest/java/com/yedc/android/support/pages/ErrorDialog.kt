package com.yedc.android.support.pages

import com.yedc.strings.R

class ErrorDialog : com.yedc.android.support.pages.OkDialog() {
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
