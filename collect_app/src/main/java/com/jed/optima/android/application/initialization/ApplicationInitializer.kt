package com.jed.optima.android.application.initialization

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.startup.AppInitializer
import net.danlew.android.joda.JodaTimeInitializer
import com.jed.optima.analytics.Analytics
import com.jed.optima.android.BuildConfig
import com.jed.optima.android.application.initialization.upgrade.UpgradeInitializer
import com.jed.optima.android.entities.EntitiesRepositoryProvider
import com.jed.optima.android.projects.ProjectsDataService
import com.jed.optima.androidshared.ui.ToastUtils
import com.jed.optima.metadata.PropertyManager
import com.jed.optima.projects.ProjectsRepository
import com.jed.optima.settings.SettingsProvider
import timber.log.Timber
import java.util.Locale

class ApplicationInitializer(
    private val context: Application,
    private val propertyManager: PropertyManager,
    private val analytics: Analytics,
    private val upgradeInitializer: UpgradeInitializer,
    private val analyticsInitializer: AnalyticsInitializer,
    private val mapsInitializer: MapsInitializer,
    private val projectsRepository: ProjectsRepository,
    private val settingsProvider: SettingsProvider,
    private val entitiesRepositoryProvider: EntitiesRepositoryProvider,
    private val projectsDataService: ProjectsDataService
) {
    fun initialize() {
        initializeLocale()
        runInitializers()
        initializeFrameworks()
    }

    private fun runInitializers() {
        upgradeInitializer.initialize()
        analyticsInitializer.initialize()
        UserPropertiesInitializer(
            analytics,
            projectsRepository,
            settingsProvider,
            context
        ).initialize()
        mapsInitializer.initialize()
        JavaRosaInitializer(propertyManager, projectsDataService, entitiesRepositoryProvider, settingsProvider).initialize()
        SystemThemeMismatchFixInitializer(context).initialize()
    }

    private fun initializeFrameworks() {
        ToastUtils.setApplication(context)
        initializeLogging()
        AppInitializer.getInstance(context).initializeComponent(JodaTimeInitializer::class.java)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    private fun initializeLocale() {
        com.jed.optima.android.application.Collect.defaultSysLanguage = Locale.getDefault().language
    }

    private fun initializeLogging() {
        if (BuildConfig.BUILD_TYPE == "odkCollectRelease") {
            Timber.plant(CrashReportingTree(analytics))
        } else {
            Timber.plant(Timber.DebugTree())
        }
    }
}
