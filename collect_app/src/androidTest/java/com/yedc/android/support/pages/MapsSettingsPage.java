package com.yedc.android.support.pages;

public class MapsSettingsPage extends Page<MapsSettingsPage> {

    @Override
    public MapsSettingsPage assertOnPage() {
        assertText(com.yedc.strings.R.string.maps);
        return this;
    }
}
