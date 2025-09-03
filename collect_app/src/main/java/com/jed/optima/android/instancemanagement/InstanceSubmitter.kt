package com.jed.optima.android.instancemanagement

import com.jed.optima.analytics.Analytics
import com.jed.optima.android.analytics.AnalyticsEvents
import com.jed.optima.android.upload.FormUploadException
import com.jed.optima.android.utilities.FormsRepositoryProvider
import com.jed.optima.android.utilities.InstanceAutoDeleteChecker
import com.jed.optima.android.utilities.InstancesRepositoryProvider
import com.jed.optima.metadata.PropertyManager
import com.jed.optima.metadata.PropertyManager.Companion.PROPMGR_DEVICE_ID
import com.jed.optima.settings.keys.ProjectKeys
import com.jed.optima.shared.settings.Settings
import timber.log.Timber

class InstanceSubmitter(
    private val formsRepository: com.jed.optima.forms.FormsRepository,
    private val generalSettings: Settings,
    private val propertyManager: PropertyManager,
    private val httpInterface: com.jed.optima.openrosa.http.OpenRosaHttpInterface,
    private val instancesRepository: com.jed.optima.forms.instances.InstancesRepository
) {

    fun submitInstances(toUpload: List<com.jed.optima.forms.instances.Instance>): Map<com.jed.optima.forms.instances.Instance, FormUploadException?> {
        val result = mutableMapOf<com.jed.optima.forms.instances.Instance, FormUploadException?>()
        val deviceId = propertyManager.getSingularProperty(PROPMGR_DEVICE_ID)

        val uploader = setUpODKUploader()

        for (instance in toUpload.sortedBy { it.finalizationDate }) {
            try {
                val destinationUrl = uploader.getUrlToSubmitTo(instance, deviceId, null, null)
                uploader.uploadOneSubmission(instance, destinationUrl)
                result[instance] = null

                deleteInstance(instance)
                logUploadedForm(instance)
            } catch (e: FormUploadException) {
                Timber.d(e)
                result[instance] = e
            }
        }
        return result
    }

    private fun setUpODKUploader(): com.jed.optima.android.upload.InstanceUploader {
        return com.jed.optima.android.upload.InstanceServerUploader(
            httpInterface,
            com.jed.optima.android.utilities.WebCredentialsUtils(generalSettings),
            generalSettings,
            instancesRepository
        )
    }

    private fun deleteInstance(instance: com.jed.optima.forms.instances.Instance) {
        // If the submission was successful, delete the instance if either the app-level
        // delete preference is set or the form definition requests auto-deletion.
        // TODO: this could take some time so might be better to do in a separate process,
        // perhaps another worker. It also feels like this could fail and if so should be
        // communicated to the user. Maybe successful delete should also be communicated?
        if (InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, generalSettings.getBoolean(ProjectKeys.KEY_DELETE_AFTER_SEND), instance)) {
            InstanceDeleter(
                InstancesRepositoryProvider(com.jed.optima.android.application.Collect.getInstance()).create(),
                FormsRepositoryProvider(com.jed.optima.android.application.Collect.getInstance()).create()
            ).delete(instance.dbId)
        }
    }

    private fun logUploadedForm(instance: com.jed.optima.forms.instances.Instance) {
        val value = com.jed.optima.android.application.Collect.getFormIdentifierHash(instance.formId, instance.formVersion)

        Analytics.log(AnalyticsEvents.SUBMISSION, "HTTP auto", value)
    }
}
