package com.jed.optima.android.support.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText

class ManualProjectCreatorDialogPage : Page<ManualProjectCreatorDialogPage>() {
    override fun assertOnPage(): ManualProjectCreatorDialogPage {
        assertText(com.jed.optima.strings.R.string.add_project)
        return this
    }

    fun inputUrl(url: String): ManualProjectCreatorDialogPage {
        inputText(com.jed.optima.strings.R.string.server_url, url)
        return this
    }

    fun inputUsername(username: String): ManualProjectCreatorDialogPage {
        inputText(com.jed.optima.strings.R.string.username, username)
        return this
    }

    fun inputPassword(password: String): ManualProjectCreatorDialogPage {
        inputText(com.jed.optima.strings.R.string.password, password)
        return this
    }

    fun addProject(): com.jed.optima.android.support.pages.MainMenuPage {
        tryAgainOnFail {
            clickOnString(com.jed.optima.strings.R.string.add)
            com.jed.optima.android.support.pages.MainMenuPage().assertOnPage()
        }

        return com.jed.optima.android.support.pages.MainMenuPage()
    }

    fun addProjectAndAssertDuplicateDialogShown(): ManualProjectCreatorDialogPage {
        onView(withText(com.jed.optima.strings.R.string.add)).perform(click())
        assertText(com.jed.optima.strings.R.string.duplicate_project_details)
        return this
    }

    fun switchToExistingProject(): com.jed.optima.android.support.pages.MainMenuPage {
        clickOnString(com.jed.optima.strings.R.string.switch_to_existing)
        return com.jed.optima.android.support.pages.MainMenuPage().assertOnPage()
    }

    fun addDuplicateProject(): com.jed.optima.android.support.pages.MainMenuPage {
        clickOnString(com.jed.optima.strings.R.string.add_duplicate_project)
        return com.jed.optima.android.support.pages.MainMenuPage().assertOnPage()
    }
}
