package com.yedc.android.application.initialization.upgrade

import android.content.Context
import com.yedc.android.BuildConfig
import com.yedc.android.application.initialization.CachedFormsCleaner
import com.yedc.android.application.initialization.ExistingProjectMigrator
import com.yedc.android.application.initialization.ExistingSettingsMigrator
import com.yedc.android.application.initialization.GoogleDriveProjectsDeleter
import com.yedc.android.application.initialization.SavepointsImporter
import com.yedc.android.application.initialization.ScheduledWorkUpgrade
import com.yedc.settings.SettingsProvider
import com.yedc.settings.keys.MetaKeys
import com.yedc.upgrade.AppUpgrader

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
