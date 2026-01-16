package com.yedc.android.application

import com.yedc.android.analytics.AnalyticsUtils
import com.yedc.android.formmanagement.FormsDataService
import com.yedc.metadata.PropertyManager
import com.yedc.settings.importing.SettingsChangeHandler
import com.yedc.settings.keys.ProjectKeys

class CollectSettingsChangeHandler(
    private val propertyManager: PropertyManager,
    private val formUpdateScheduler: _root_ide_package_.com.yedc.android.backgroundwork.FormUpdateScheduler,
    private val formsDataService: FormsDataService
) : SettingsChangeHandler {

    override fun onSettingChanged(projectId: String, newValue: Any?, changedKey: String) {
        propertyManager.reload()

        if (changedKey == ProjectKeys.KEY_SERVER_URL) {
            formsDataService.clear(projectId)
        }

        if (changedKey == ProjectKeys.KEY_FORM_UPDATE_MODE ||
            changedKey == ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK
        ) {
            formUpdateScheduler.scheduleUpdates(projectId)
        }

        if (changedKey == ProjectKeys.KEY_SERVER_URL) {
            AnalyticsUtils.logServerConfiguration(newValue.toString())
        }
    }

    override fun onSettingsChanged(
        projectId: String,
        changedUnprotectedKeys: List<String>,
        changedProtectedKeys: List<String>
    ) {
        propertyManager.reload()
        if (changedUnprotectedKeys.contains(ProjectKeys.KEY_FORM_UPDATE_MODE) || changedUnprotectedKeys.contains(ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)) {
            formUpdateScheduler.scheduleUpdates(projectId)
        }
    }
}
