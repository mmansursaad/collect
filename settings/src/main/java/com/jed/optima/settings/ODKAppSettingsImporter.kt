package com.jed.optima.settings

import org.json.JSONObject
import com.jed.optima.projects.Project
import com.jed.optima.projects.ProjectConfigurationResult
import com.jed.optima.projects.ProjectsRepository
import com.jed.optima.settings.importing.ProjectDetailsCreatorImpl
import com.jed.optima.settings.importing.SettingsChangeHandler
import com.jed.optima.settings.importing.SettingsImporter
import com.jed.optima.settings.validation.JsonSchemaSettingsValidator

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
        _root_ide_package_.com.jed.optima.settings.ODKAppSettingsMigrator(settingsProvider.getMetaSettings()),
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
