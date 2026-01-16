package com.yedc.android.support.pages

class SelectMinimalDialogPage(private val formName: String) : Page<SelectMinimalDialogPage>() {
    override fun assertOnPage(): SelectMinimalDialogPage {
        assertTextDoesNotExistInDialog(formName)
        return this
    }

    fun selectItem(item: String): com.yedc.android.support.pages.FormEntryPage {
        return clickOnTextInDialog(item,
            com.yedc.android.support.pages.FormEntryPage(formName)
        )
    }
}
