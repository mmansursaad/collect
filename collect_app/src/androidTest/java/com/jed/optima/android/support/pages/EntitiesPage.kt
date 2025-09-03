package com.jed.optima.android.support.pages

class EntitiesPage : Page<EntitiesPage>() {

    override fun assertOnPage(): EntitiesPage {
        assertToolbarTitle(com.jed.optima.strings.R.string.entities_title)
        return this
    }

    fun clickOnList(list: String): EntityListPage {
        clickOnText(list)
        return EntityListPage(list)
    }
}
