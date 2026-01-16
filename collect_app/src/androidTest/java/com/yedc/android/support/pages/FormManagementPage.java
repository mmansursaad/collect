package com.yedc.android.support.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.not;

import androidx.test.espresso.contrib.RecyclerViewActions;

public class FormManagementPage extends Page<FormManagementPage> {

    @Override
    public FormManagementPage assertOnPage() {
        assertToolbarTitle(getTranslatedString(com.yedc.strings.R.string.form_management_preferences));
        return this;
    }

    public ListPreferenceDialog<FormManagementPage> clickUpdateForms() {
        clickOnString(com.yedc.strings.R.string.form_update_mode_title);
        return new ListPreferenceDialog<>(com.yedc.strings.R.string.form_update_mode_title, this).assertOnPage();
    }

    public ListPreferenceDialog<FormManagementPage> clickAutomaticUpdateFrequency() {
        clickOnString(com.yedc.strings.R.string.form_update_frequency_title);
        return new ListPreferenceDialog<>(com.yedc.strings.R.string.form_update_frequency_title, this).assertOnPage();
    }

    public ListPreferenceDialog<FormManagementPage> clickAutoSend() {
        clickOnString(com.yedc.strings.R.string.autosend);
        return new ListPreferenceDialog<>(com.yedc.strings.R.string.autosend, this).assertOnPage();
    }

    public FormManagementPage openShowGuidanceForQuestions() {
        scrollToRecyclerViewItemAndClickText(getTranslatedString(com.yedc.strings.R.string.guidance_hint_title));
        return this;
    }

    public FormManagementPage openConstraintProcessing() {
        scrollToRecyclerViewItemAndClickText(getTranslatedString(com.yedc.strings.R.string.constraint_behavior_title));
        return this;
    }

    public FormManagementPage scrollToConstraintProcessing() {
        onView(withId(androidx.preference.R.id.recycler_view)).perform(RecyclerViewActions
                .actionOnItem(hasDescendant(withText(getTranslatedString(com.yedc.strings.R.string.constraint_behavior_title))), scrollTo()));
        return this;
    }

    public FormManagementPage checkIfConstraintProcessingIsDisabled() {
        onView(withText(getTranslatedString(com.yedc.strings.R.string.constraint_behavior_title))).check(matches(not(isEnabled())));
        return this;
    }

    public FormManagementPage checkIfConstraintProcessingIsEnabled() {
        onView(withText(getTranslatedString(com.yedc.strings.R.string.constraint_behavior_title))).check(matches(isEnabled()));
        return this;
    }
}
