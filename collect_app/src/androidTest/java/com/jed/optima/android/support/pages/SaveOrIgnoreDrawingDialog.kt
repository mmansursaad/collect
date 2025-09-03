package com.jed.optima.android.support.pages

class SaveOrIgnoreDrawingDialog<D : Page<D>>(
    private val drawingName: String,
    private val destination: D
) : Page<SaveOrIgnoreDrawingDialog<D>>() {

    override fun assertOnPage(): SaveOrIgnoreDrawingDialog<D> {
        val title = getTranslatedString(com.jed.optima.strings.R.string.exit) + " " + drawingName
        assertText(title)
        return this
    }

    fun clickSaveChanges(): D {
        return clickOnTextInDialog(com.jed.optima.strings.R.string.keep_changes, destination)
    }

    fun clickDiscardChanges(): D {
        return clickOnTextInDialog(com.jed.optima.strings.R.string.discard_changes, destination)
    }
}
