package com.jed.optima.android.support.rules

class BlankFormTestRule @JvmOverloads constructor(
    private val formFilename: String,
    private val formName: String,
    private val mediaFilePaths: List<String>? = null
) : FormEntryActivityTestRule() {

    private lateinit var formEntryPage: com.jed.optima.android.support.pages.FormEntryPage

    override fun before() {
        super.before()
        setUpProjectAndCopyForm(formFilename, mediaFilePaths)
        formEntryPage = fillNewForm(formFilename, formName)
    }

    fun startInFormEntry(): com.jed.optima.android.support.pages.FormEntryPage {
        return formEntryPage
    }
}
