package com.jed.optima.android.support.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class DeleteSavedFormPage extends Page<DeleteSavedFormPage> {

    @Override
    public DeleteSavedFormPage assertOnPage() {
        assertToolbarTitle(getTranslatedString(com.jed.optima.strings.R.string.manage_files));
        return this;
    }

    public DeleteSavedFormPage clickBlankForms() {
        clickOnString(com.jed.optima.strings.R.string.forms);
        return this;
    }

    public DeleteSavedFormPage clickForm(String formName) {
        onView(withText(formName)).perform(scrollTo(), click());
        return this;
    }

    public DeleteSelectedDialog clickDeleteSelected(int numberSelected) {
        clickOnString(com.jed.optima.strings.R.string.delete_file);
        return new DeleteSelectedDialog(numberSelected, this).assertOnPage();
    }
}
