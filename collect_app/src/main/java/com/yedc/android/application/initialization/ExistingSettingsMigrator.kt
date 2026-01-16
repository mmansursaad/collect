package com.yedc.android.application.initialization

import com.yedc.projects.ProjectsRepository
import com.yedc.settings.SettingsProvider
import com.yedc.upgrade.Upgrade

class ExistingSettingsMigrator(
    private val projectsRepository: ProjectsRepository,
    private val settingsProvider: SettingsProvider,
    private val settingsMigrator: com.yedc.settings.ODKAppSettingsMigrator
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
