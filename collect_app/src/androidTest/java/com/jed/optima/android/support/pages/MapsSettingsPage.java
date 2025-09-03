package com.jed.optima.android.support.pages;

public class MapsSettingsPage extends Page<MapsSettingsPage> {

    @Override
    public MapsSettingsPage assertOnPage() {
        assertText(com.jed.optima.strings.R.string.maps);
        return this;
    }
}
