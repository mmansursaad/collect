package com.yedc.android.support.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText

class ManualProjectCreatorDialogPage : Page<ManualProjectCreatorDialogPage>() {
    override fun assertOnPage(): ManualProjectCreatorDialogPage {
        assertText(com.yedc.strings.R.string.add_project)
        return this
    }

    fun inputUrl(url: String): ManualProjectCreatorDialogPage {
        inputText(com.yedc.strings.R.string.server_url, url)
        return this
    }

    fun inputUsername(username: String): ManualProjectCreatorDialogPage {
        inputText(com.yedc.strings.R.string.username, username)
        return this
    }

    fun inputPassword(password: String): ManualProjectCreatorDialogPage {
        inputText(com.yedc.strings.R.string.password, password)
        return this
    }

    fun addProject(): com.yedc.android.support.pages.MainMenuPage {
        tryAgainOnFail {
            clickOnString(com.yedc.strings.R.string.add)
            com.yedc.android.support.pages.MainMenuPage().assertOnPage()
        }

        return com.yedc.android.support.pages.MainMenuPage()
    }

    fun addProjectAndAssertDuplicateDialogShown(): ManualProjectCreatorDialogPage {
        onView(withText(com.yedc.strings.R.string.add)).perform(click())
        assertText(com.yedc.strings.R.string.duplicate_project_details)
        return this
    }

    fun switchToExistingProject(): com.yedc.android.support.pages.MainMenuPage {
        clickOnString(com.yedc.strings.R.string.switch_to_existing)
        return com.yedc.android.support.pages.MainMenuPage().assertOnPage()
    }

    fun addDuplicateProject(): com.yedc.android.support.pages.MainMenuPage {
        clickOnString(com.yedc.strings.R.string.add_duplicate_project)
        return com.yedc.android.support.pages.MainMenuPage().assertOnPage()
    }
}
