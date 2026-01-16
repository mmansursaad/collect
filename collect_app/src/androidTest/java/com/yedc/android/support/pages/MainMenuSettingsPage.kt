package com.yedc.android.support.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import org.hamcrest.Matchers.endsWith
import com.yedc.android.support.matchers.CustomMatchers.withIndex

class MainMenuSettingsPage : Page<MainMenuSettingsPage>() {
    override fun assertOnPage(): MainMenuSettingsPage {
        assertText(com.yedc.strings.R.string.main_menu_settings)
        return this
    }

    fun assertDraftsUnchecked(): MainMenuSettingsPage {
        onView(withIndex(withClassName(endsWith("CheckBox")), 0)).check(matches(isNotChecked()))
        return this
    }
}
