package com.jed.optima.android.support.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.CoreMatchers.allOf
import com.jed.optima.android.support.matchers.CustomMatchers.withIndex
import com.jed.optima.strings.R

class SendFinalizedFormPage : Page<SendFinalizedFormPage>() {
    override fun assertOnPage(): SendFinalizedFormPage {
        onView(
            allOf(
                withText(getTranslatedString(R.string.send_data)),
                isDescendantOfA(withId(com.jed.optima.androidshared.R.id.toolbar))
            )
        ).check(matches(isDisplayed()))
        return this
    }

    fun clickOnForm(formLabel: String): ViewFormPage {
        clickOnText(formLabel)
        return ViewFormPage(formLabel).assertOnPage()
    }

    fun clickOnForm(formName: String, instanceName: String): ViewFormPage {
        clickOnText(instanceName)
        return ViewFormPage(formName).assertOnPage()
    }

    fun clickSendSelected(): com.jed.optima.android.support.pages.OkDialog {
        clickOnText(getTranslatedString(R.string.send_selected_data))
        return com.jed.optima.android.support.pages.OkDialog()
    }

    fun clickSendSelectedWithAuthenticationError(): com.jed.optima.android.support.pages.ServerAuthDialog {
        clickOnText(getTranslatedString(R.string.send_selected_data))
        return com.jed.optima.android.support.pages.ServerAuthDialog().assertOnPage()
    }

    fun clickSelectAll(): SendFinalizedFormPage {
        clickOnString(R.string.select_all)
        return this
    }

    @Deprecated("uses the deprecated {@link com.jed.optima.android.support.matchers.CustomMatchers#withIndex(Matcher, int)})} helper.")
    fun selectForm(index: Int): SendFinalizedFormPage {
        onView(withIndex(ViewMatchers.withId(androidx.appcompat.R.id.checkbox), index)).perform(click())
        return this
    }

    fun sortByDateOldestFirst(): SendFinalizedFormPage {
        onView(withId(com.jed.optima.android.R.id.menu_sort)).perform(click())
        clickOnString(R.string.sort_by_date_asc)
        return this
    }

    fun sortByDateNewestFirst(): SendFinalizedFormPage {
        onView(withId(com.jed.optima.android.R.id.menu_sort)).perform(click())
        clickOnString(R.string.sort_by_date_desc)
        return this
    }
}
