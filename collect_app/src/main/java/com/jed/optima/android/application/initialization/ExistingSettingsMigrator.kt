package com.jed.optima.android.application.initialization

import com.jed.optima.projects.ProjectsRepository
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.upgrade.Upgrade

class ExistingSettingsMigrator(
    private val projectsRepository: ProjectsRepository,
    private val settingsProvider: SettingsProvider,
    private val settingsMigrator: com.jed.optima.settings.ODKAppSettingsMigrator
) : Upgrade {

    override fun key(): String? {
        return null
    }

    override fun run() {
        projectsRepository.getAll().forEach {
            settingsMigrator.migrate(
                settingsProvider.getUnprotectedSettings(it.uuid),
                settingsProvider.getProtectedSettings(it.uuid)
            )
        }
    }
}
