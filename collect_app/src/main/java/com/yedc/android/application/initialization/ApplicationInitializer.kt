package com.yedc.android.application.initialization

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.startup.AppInitializer
import net.danlew.android.joda.JodaTimeInitializer
import com.yedc.analytics.Analytics
import com.yedc.android.BuildConfig
import com.yedc.android.application.initialization.upgrade.UpgradeInitializer
import com.yedc.android.entities.EntitiesRepositoryProvider
import com.yedc.android.projects.ProjectsDataService
import com.yedc.androidshared.ui.ToastUtils
import com.yedc.metadata.PropertyManager
import com.yedc.projects.ProjectsRepository
import com.yedc.settings.SettingsProvider
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
        _root_ide_package_.com.yedc.android.application.Collect.defaultSysLanguage = Locale.getDefault().language
    }

    private fun initializeLogging() {
        if (BuildConfig.BUILD_TYPE == "odkCollectRelease") {
            Timber.plant(CrashReportingTree(analytics))
        } else {
            Timber.plant(Timber.DebugTree())
        }
    }
}
