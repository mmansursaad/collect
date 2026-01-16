package com.yedc.android.instancemanagement

import com.yedc.analytics.Analytics
import com.yedc.android.analytics.AnalyticsEvents
import com.yedc.android.upload.FormUploadException
import com.yedc.android.utilities.FormsRepositoryProvider
import com.yedc.android.utilities.InstanceAutoDeleteChecker
import com.yedc.android.utilities.InstancesRepositoryProvider
import com.yedc.metadata.PropertyManager
import com.yedc.metadata.PropertyManager.Companion.PROPMGR_DEVICE_ID
import com.yedc.settings.keys.ProjectKeys
import com.yedc.shared.settings.Settings
import timber.log.Timber

class InstanceSubmitter(
    private val formsRepository: com.yedc.forms.FormsRepository,
    private val generalSettings: Settings,
    private val propertyManager: PropertyManager,
    private val httpInterface: com.yedc.openrosa.http.OpenRosaHttpInterface,
    private val instancesRepository: com.yedc.forms.instances.InstancesRepository
) {

    fun submitInstances(toUpload: List<com.yedc.forms.instances.Instance>): Map<com.yedc.forms.instances.Instance, FormUploadException?> {
        val result = mutableMapOf<com.yedc.forms.instances.Instance, FormUploadException?>()
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

    private fun setUpODKUploader(): _root_ide_package_.com.yedc.android.upload.InstanceUploader {
        return _root_ide_package_.com.yedc.android.upload.InstanceServerUploader(
            httpInterface,
            _root_ide_package_.com.yedc.android.utilities.WebCredentialsUtils(generalSettings),
            generalSettings,
            instancesRepository
        )
    }

    private fun deleteInstance(instance: com.yedc.forms.instances.Instance) {
        // If the submission was successful, delete the instance if either the app-level
        // delete preference is set or the form definition requests auto-deletion.
        // TODO: this could take some time so might be better to do in a separate process,
        // perhaps another worker. It also feels like this could fail and if so should be
        // communicated to the user. Maybe successful delete should also be communicated?
        if (InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, generalSettings.getBoolean(ProjectKeys.KEY_DELETE_AFTER_SEND), instance)) {
            InstanceDeleter(
                InstancesRepositoryProvider(_root_ide_package_.com.yedc.android.application.Collect.getInstance()).create(),
                FormsRepositoryProvider(_root_ide_package_.com.yedc.android.application.Collect.getInstance()).create()
            ).delete(instance.dbId)
        }
    }

    private fun logUploadedForm(instance: com.yedc.forms.instances.Instance) {
        val value = _root_ide_package_.com.yedc.android.application.Collect.getFormIdentifierHash(instance.formId, instance.formVersion)

        Analytics.log(AnalyticsEvents.SUBMISSION, "HTTP auto", value)
    }
}
