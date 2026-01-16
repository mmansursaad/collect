package com.yedc.android.support.pages;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.equalTo;

public class UserInterfacePage extends Page<UserInterfacePage> {

    @Override
    public UserInterfacePage assertOnPage() {
        assertText(com.yedc.strings.R.string.client);
        return this;
    }

    public UserInterfacePage clickOnLanguage() {
        onView(withText(getTranslatedString(com.yedc.strings.R.string.language))).perform(click());
        return this;
    }

    public MainMenuPage clickOnSelectedLanguage(String language) {
        onData(equalTo(language)).perform(click());
        return new MainMenuPage().assertOnPage();
    }

    public UserInterfacePage clickNavigation() {
        clickOnString(com.yedc.strings.R.string.navigation);
        return this;
    }

    public UserInterfacePage clickUseSwipesAndButtons() {
        clickOnString(com.yedc.strings.R.string.swipe_buttons_navigation);
        return this;
    }

    public UserInterfacePage clickOnTheme() {
        onView(withText(getTranslatedString(com.yedc.strings.R.string.app_theme))).perform(click());
        return this;
    }

    public UserInterfacePage clickUseNavigationButtons() {
        clickOnString(com.yedc.strings.R.string.buttons_navigation);
        return this;
    }

    public UserInterfacePage clickSwipes() {
        clickOnString(com.yedc.strings.R.string.swipe_navigation);
        return this;
    }
}
