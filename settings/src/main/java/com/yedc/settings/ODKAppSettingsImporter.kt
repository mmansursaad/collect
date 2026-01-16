package com.yedc.settings

import org.json.JSONObject
import com.yedc.projects.Project
import com.yedc.projects.ProjectConfigurationResult
import com.yedc.projects.ProjectsRepository
import com.yedc.settings.importing.ProjectDetailsCreatorImpl
import com.yedc.settings.importing.SettingsChangeHandler
import com.yedc.settings.importing.SettingsImporter
import com.yedc.settings.validation.JsonSchemaSettingsValidator

class ODKAppSettingsImporter(
    projectsRepository: ProjectsRepository,
    settingsProvider: SettingsProvider,
    generalDefaults: Map<String, Any>,
    adminDefaults: Map<String, Any>,
    projectColors: List<String>,
    settingsChangedHandler: SettingsChangeHandler,
    private val deviceUnsupportedSettings: JSONObject
) {

    private val settingsImporter = SettingsImporter(
        settingsProvider,
        _root_ide_package_.com.yedc.settings.ODKAppSettingsMigrator(settingsProvider.getMetaSettings()),
        JsonSchemaSettingsValidator { javaClass.getResourceAsStream("/client-settings.schema.json")!! },
        generalDefaults,
        adminDefaults,
        settingsChangedHandler,
        projectsRepository,
        ProjectDetailsCreatorImpl(projectColors, generalDefaults)
    )

    fun fromJSON(json: String, project: Project.Saved): ProjectConfigurationResult {
        return try {
            settingsImporter.fromJSON(json, project, deviceUnsupportedSettings)
        } catch (e: Throwable) {
            ProjectConfigurationResult.INVALID_SETTINGS
        }
    }
}
