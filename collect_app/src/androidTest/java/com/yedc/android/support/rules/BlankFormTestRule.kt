package com.yedc.android.support.rules

class BlankFormTestRule @JvmOverloads constructor(
    private val formFilename: String,
    private val formName: String,
    private val mediaFilePaths: List<String>? = null
) : FormEntryActivityTestRule() {

    private lateinit var formEntryPage: com.yedc.android.support.pages.FormEntryPage

    override fun before() {
        super.before()
        setUpProjectAndCopyForm(formFilename, mediaFilePaths)
        formEntryPage = fillNewForm(formFilename, formName)
    }

    fun startInFormEntry(): com.yedc.android.support.pages.FormEntryPage {
        return formEntryPage
    }
}
