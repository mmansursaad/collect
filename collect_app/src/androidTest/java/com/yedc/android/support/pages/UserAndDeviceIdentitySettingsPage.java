package com.yedc.android.support.pages;

public class UserAndDeviceIdentitySettingsPage extends Page<UserAndDeviceIdentitySettingsPage> {

    @Override
    public UserAndDeviceIdentitySettingsPage assertOnPage() {
        assertText(com.yedc.strings.R.string.user_and_device_identity_title);
        return this;
    }

    public FormMetadataPage clickFormMetadata() {
        clickOnString(com.yedc.strings.R.string.form_metadata);
        return new FormMetadataPage();
    }
}
