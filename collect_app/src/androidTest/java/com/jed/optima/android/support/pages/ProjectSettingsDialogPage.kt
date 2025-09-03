package com.jed.optima.android.support.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers.allOf

internal class ProjectSettingsDialogPage : Page<ProjectSettingsDialogPage>() {

    override fun assertOnPage(): ProjectSettingsDialogPage {
        assertText(com.jed.optima.strings.R.string.projects)
        return this
    }

    fun clickSettings(): com.jed.optima.android.support.pages.ProjectSettingsPage {
        return clickOnTextInDialog(
            com.jed.optima.strings.R.string.settings,
            com.jed.optima.android.support.pages.ProjectSettingsPage()
        )
    }

    fun clickAbout(): com.jed.optima.android.support.pages.AboutPage {
        return clickOnTextInDialog(
            com.jed.optima.strings.R.string.about_preferences,
            com.jed.optima.android.support.pages.AboutPage()
        )
    }

    fun clickAddProject(): QrCodeProjectCreatorDialogPage {
        return clickOnTextInDialog(
            com.jed.optima.strings.R.string.add_project,
            QrCodeProjectCreatorDialogPage()
        )
    }

    fun assertCurrentProject(projectName: String, subtext: String): ProjectSettingsDialogPage {
        onView(
            allOf(
                hasDescendant(withText(projectName)),
                hasDescendant(withText(subtext)),
                withContentDescription(
                    getTranslatedString(
                        com.jed.optima.strings.R.string.using_project,
                        projectName
                    )
                )
            )
        ).check(matches(isDisplayed()))
        return this
    }

    fun assertInactiveProject(projectName: String, subtext: String): ProjectSettingsDialogPage {
        onView(
            allOf(
                hasDescendant(withText(projectName)),
                hasDescendant(withText(subtext)),
                withContentDescription(
                    getTranslatedString(
                        com.jed.optima.strings.R.string.switch_to_project,
                        projectName
                    )
                )
            )
        ).check(matches(isDisplayed()))
        return this
    }

    fun assertNotInactiveProject(projectName: String): ProjectSettingsDialogPage {
        onView(
            allOf(
                hasDescendant(withText(projectName)),
                withContentDescription(
                    getTranslatedString(
                        com.jed.optima.strings.R.string.switch_to_project,
                        projectName
                    )
                )
            )
        ).check(doesNotExist())
        return this
    }

    fun selectProject(projectName: String): com.jed.optima.android.support.pages.MainMenuPage {
        waitForDialogToSettle()
        onView(
            allOf(
                hasDescendant(withText(projectName)),
                withContentDescription(
                    getTranslatedString(
                        com.jed.optima.strings.R.string.switch_to_project,
                        projectName
                    )
                )
            )
        )
            .inRoot(RootMatchers.isDialog())
            .perform(click())
        return com.jed.optima.android.support.pages.MainMenuPage().assertOnPage()
    }
}
