package com.jed.optima.android.support.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class EndOfFormPage extends Page<EndOfFormPage> {

    private final String formName;

    public EndOfFormPage(String formName) {
        this.formName = formName;
    }

    @Override
    public EndOfFormPage assertOnPage() {
        String endFormMessage = getTranslatedString(com.jed.optima.strings.R.string.save_enter_data_description, formName);
        onView(withText(endFormMessage)).check(matches(isDisplayed()));
        return this;
    }
}
