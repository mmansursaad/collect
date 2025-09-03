package com.jed.optima.android.support.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class ProjectSettingsPage extends Page<ProjectSettingsPage> {

    @Override
    public ProjectSettingsPage assertOnPage() {
        assertText(com.jed.optima.strings.R.string.project_settings);
        return this;
    }

    public ProjectDisplayPage clickProjectDisplay() {
        scrollToRecyclerViewItemAndClickText(com.jed.optima.strings.R.string.project_display_title);
        return new ProjectDisplayPage().assertOnPage();
    }

    public ProjectManagementPage clickProjectManagement() {
        scrollToRecyclerViewItemAndClickText(com.jed.optima.strings.R.string.project_management_section_title);
        return new ProjectManagementPage().assertOnPage();
    }

    public AccessControlPage clickAccessControl() {
        scrollToRecyclerViewItemAndClickText(com.jed.optima.strings.R.string.access_control_section_title);
        return new AccessControlPage().assertOnPage();
    }

    public UserInterfacePage clickOnUserInterface() {
        clickOnString(com.jed.optima.strings.R.string.client);
        return new UserInterfacePage().assertOnPage();
    }

    public FormManagementPage openFormManagement() {
        clickOnString(com.jed.optima.strings.R.string.form_management_preferences);
        return new FormManagementPage();
    }

    public ServerSettingsPage clickServerSettings() {
        clickOnString(com.jed.optima.strings.R.string.server_settings_title);
        return new ServerSettingsPage().assertOnPage();
    }

    public MapsSettingsPage clickMaps() {
        clickOnString(com.jed.optima.strings.R.string.maps);
        return new MapsSettingsPage().assertOnPage();
    }


    public UserAndDeviceIdentitySettingsPage clickUserAndDeviceIdentity() {
        clickOnString(com.jed.optima.strings.R.string.user_and_device_identity_title);
        return new UserAndDeviceIdentitySettingsPage().assertOnPage();
    }

    public ProjectSettingsPage checkIfServerOptionIsDisplayed() {
        onView(withText(getTranslatedString(com.jed.optima.strings.R.string.server_settings_title))).check(matches(isDisplayed()));
        return this;
    }

    public ProjectSettingsPage checkIfFormManagementOptionIsDisplayed() {
        onView(withText(getTranslatedString(com.jed.optima.strings.R.string.form_management_preferences))).check(matches(isDisplayed()));
        return this;
    }

    public ProjectSettingsPage checkIfServerOptionIsNotDisplayed() {
        onView(withText("Server")).check(doesNotExist());
        return this;
    }

    public FormManagementPage clickFormManagement() {
        onView(withText(getTranslatedString(com.jed.optima.strings.R.string.form_management_preferences))).perform(click());
        return new FormManagementPage();
    }

    public ExperimentalPage clickExperimental() {
        onView(withText(getTranslatedString(com.jed.optima.strings.R.string.experimental))).perform(click());
        return new ExperimentalPage().assertOnPage();
    }

    public ProjectSettingsPage setAdminPassword(String password) {
        scrollToRecyclerViewItemAndClickText(com.jed.optima.strings.R.string.set_admin_password);
        inputText(password);
        clickOKOnDialog();
        return this;
    }
}
