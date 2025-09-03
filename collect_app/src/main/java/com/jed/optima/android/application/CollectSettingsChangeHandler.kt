package com.jed.optima.android.application

import com.jed.optima.android.analytics.AnalyticsUtils
import com.jed.optima.android.formmanagement.FormsDataService
import com.jed.optima.metadata.PropertyManager
import com.jed.optima.settings.importing.SettingsChangeHandler
import com.jed.optima.settings.keys.ProjectKeys

class CollectSettingsChangeHandler(
    private val propertyManager: PropertyManager,
    private val formUpdateScheduler: com.jed.optima.android.backgroundwork.FormUpdateScheduler,
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
