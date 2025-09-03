package com.jed.optima.android.support.pages

import androidx.appcompat.widget.AppCompatImageButton
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import com.jed.optima.android.R

class ErrorPage : Page<ErrorPage>() {

    override fun assertOnPage(): ErrorPage {
        assertText(com.jed.optima.strings.R.string.errors)
        return this
    }

    fun assertError(errorMessage: String): ErrorPage {
        assertText(errorMessage)
        return this
    }

    fun navigateBack(): com.jed.optima.android.support.pages.MainMenuPage {
        onView(allOf(instanceOf(AppCompatImageButton::class.java), withParent(withId(com.jed.optima.androidshared.R.id.toolbar)))).perform(click())
        return com.jed.optima.android.support.pages.MainMenuPage()
    }
}
