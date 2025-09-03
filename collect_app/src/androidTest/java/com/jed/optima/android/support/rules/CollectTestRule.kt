package com.jed.optima.android.support.rules

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import com.jed.optima.android.external.AndroidShortcutsActivity
import com.jed.optima.android.support.ActivityHelpers.getLaunchIntent
import com.jed.optima.android.support.StubOpenRosaServer
import com.jed.optima.android.support.pages.FirstLaunchPage
import com.jed.optima.android.support.pages.Page
import com.jed.optima.androidtest.ActivityScenarioLauncherRule
import java.util.function.Consumer

class CollectTestRule @JvmOverloads constructor(
    private val useDemoProject: Boolean = true
) : ActivityScenarioLauncherRule() {

    override fun before() {
        super.before()

        val firstLaunchPage = launch(
            getLaunchIntent(),
            FirstLaunchPage()
        ).assertOnPage()

        if (useDemoProject) {
            firstLaunchPage.clickTryCollect()
        }
    }

    fun startAtMainMenu() = com.jed.optima.android.support.pages.MainMenuPage()

    fun startAtFirstLaunch() = FirstLaunchPage()

    fun withProject(serverUrl: String): com.jed.optima.android.support.pages.MainMenuPage {
        return startAtFirstLaunch()
            .clickManuallyEnterProjectDetails()
            .inputUrl(serverUrl)
            .addProject()
    }

    fun withMatchExactlyProject(serverUrl: String): com.jed.optima.android.support.pages.MainMenuPage {
        return startAtFirstLaunch()
            .clickManuallyEnterProjectDetails()
            .inputUrl(serverUrl)
            .addProject()
            .enableMatchExactly()
            .clickFillBlankForm()
            .clickRefresh()
            .pressBack(com.jed.optima.android.support.pages.MainMenuPage())
    }

    fun withProject(testServer: StubOpenRosaServer, vararg formFiles: String): com.jed.optima.android.support.pages.MainMenuPage {
        val mainMenuPage = startAtFirstLaunch()
            .clickManuallyEnterProjectDetails()
            .inputUrl(testServer.url)
            .addProject()

        return if (formFiles.isNotEmpty()) {
            formFiles.fold(mainMenuPage) { page, formFile -> page.copyForm(formFile, testServer.hostName) }
        } else {
            mainMenuPage
        }
    }

    fun launchShortcuts(): com.jed.optima.android.support.pages.ShortcutsPage {
        val scenario = launchForResult(AndroidShortcutsActivity::class.java)
        return com.jed.optima.android.support.pages.ShortcutsPage(scenario).assertOnPage()
    }

    fun <D : Page<D>> relaunch(destination: D): D {
        return launch(getLaunchIntent(), destination)
    }

    fun <T : Page<T>> launch(intent: Intent, destination: T): T {
        launch<Activity>(intent)
        return destination.assertOnPage()
    }

    fun <T : Page<T>> launchForResult(
        intent: Intent,
        destination: T,
        actions: Consumer<T>
    ): Instrumentation.ActivityResult {
        val scenario = launchForResult<Activity>(intent)
        destination.async().assertOnPage()
        actions.accept(destination)
        return scenario.result
    }
}
