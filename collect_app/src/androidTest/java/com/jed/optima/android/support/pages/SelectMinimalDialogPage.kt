package com.jed.optima.android.support.pages

class SelectMinimalDialogPage(private val formName: String) : Page<SelectMinimalDialogPage>() {
    override fun assertOnPage(): SelectMinimalDialogPage {
        assertTextDoesNotExistInDialog(formName)
        return this
    }

    fun selectItem(item: String): com.jed.optima.android.support.pages.FormEntryPage {
        return clickOnTextInDialog(item,
            com.jed.optima.android.support.pages.FormEntryPage(formName)
        )
    }
}
