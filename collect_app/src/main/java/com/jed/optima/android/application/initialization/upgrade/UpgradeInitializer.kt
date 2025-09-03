package com.jed.optima.android.application.initialization.upgrade

import android.content.Context
import com.jed.optima.android.BuildConfig
import com.jed.optima.android.application.initialization.CachedFormsCleaner
import com.jed.optima.android.application.initialization.ExistingProjectMigrator
import com.jed.optima.android.application.initialization.ExistingSettingsMigrator
import com.jed.optima.android.application.initialization.GoogleDriveProjectsDeleter
import com.jed.optima.android.application.initialization.SavepointsImporter
import com.jed.optima.android.application.initialization.ScheduledWorkUpgrade
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.settings.keys.MetaKeys
import com.jed.optima.upgrade.AppUpgrader

class UpgradeInitializer(
    private val context: Context,
    private val settingsProvider: SettingsProvider,
    private val existingProjectMigrator: ExistingProjectMigrator,
    private val existingSettingsMigrator: ExistingSettingsMigrator,
    private val scheduledWorkUpgrade: ScheduledWorkUpgrade,
    private val googleDriveProjectsDeleter: GoogleDriveProjectsDeleter,
    private val savepointsImporter: SavepointsImporter,
    private val cachedFormsCleaner: CachedFormsCleaner
) {

    fun initialize() {
        AppUpgrader(
            MetaKeys.LAST_LAUNCHED,
            settingsProvider.getMetaSettings(),
            BuildConfig.VERSION_CODE,
            BeforeProjectsInstallDetector(context),
            listOf(
                existingProjectMigrator,
                existingSettingsMigrator,
                scheduledWorkUpgrade,
                googleDriveProjectsDeleter,
                savepointsImporter,
                cachedFormsCleaner
            )
        ).upgradeIfNeeded()
    }
}
