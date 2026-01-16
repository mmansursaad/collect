package com.yedc.android.support.pages;

public class ExperimentalPage extends Page<ExperimentalPage> {

    @Override
    public ExperimentalPage assertOnPage() {
        assertToolbarTitle(getTranslatedString(com.yedc.strings.R.string.experimental));
        return this;
    }
}
