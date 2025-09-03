package com.jed.optima.android.support.pages;

public class ExperimentalPage extends Page<ExperimentalPage> {

    @Override
    public ExperimentalPage assertOnPage() {
        assertToolbarTitle(getTranslatedString(com.jed.optima.strings.R.string.experimental));
        return this;
    }
}
