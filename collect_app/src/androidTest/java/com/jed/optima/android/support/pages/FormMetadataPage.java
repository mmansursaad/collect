package com.jed.optima.android.support.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class FormMetadataPage extends PreferencePage<FormMetadataPage> {

    @Override
    public FormMetadataPage assertOnPage() {
        assertText(com.jed.optima.strings.R.string.form_metadata_title);
        return this;
    }

    public FormMetadataPage clickEmail() {
        onView(withText(getTranslatedString(com.jed.optima.strings.R.string.email))).perform(click());
        return this;
    }

    public FormMetadataPage clickUsername() {
        onView(withText(getTranslatedString(com.jed.optima.strings.R.string.username))).perform(click());
        return this;
    }

    public FormMetadataPage clickPhoneNumber() {
        onView(withText(getTranslatedString(com.jed.optima.strings.R.string.phone_number))).perform(click());
        return this;
    }
}
