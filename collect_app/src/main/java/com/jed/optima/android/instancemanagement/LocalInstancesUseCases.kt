package com.jed.optima.android.instancemanagement

import com.jed.optima.analytics.Analytics
import com.jed.optima.android.analytics.AnalyticsEvents
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object LocalInstancesUseCases {
    @JvmOverloads
    @JvmStatic
    fun createInstanceFile(
        formName: String,
        instancesDir: String,
        timezone: TimeZone = TimeZone.getDefault(),
        clock: () -> Long = { System.currentTimeMillis() }
    ): File? {
        val sanitizedFormName = com.jed.optima.android.utilities.FormNameUtils.formatFilenameFromFormName(formName)

        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH).also {
            it.timeZone = timezone
        }.format(Date(clock()))

        val instanceDir = instancesDir + File.separator + sanitizedFormName + "_" + timestamp

        if (com.jed.optima.android.utilities.FileUtils.createFolder(instanceDir)) {
            return File(instanceDir + File.separator + sanitizedFormName + "_" + timestamp + ".xml")
        } else {
            Timber.e(Error("Error creating form instance file"))
            return null
        }
    }

    fun editInstance(
        instanceFilePath: String,
        instancesDir: String,
        instancesRepository: com.jed.optima.forms.instances.InstancesRepository,
        formsRepository: com.jed.optima.forms.FormsRepository,
        clock: () -> Long = { System.currentTimeMillis() }
    ): InstanceEditResult {
        val sourceInstance = instancesRepository.getOneByPath(instanceFilePath)!!

        val latestEditInstance = findLatestEditIfExists(sourceInstance, instancesRepository)
        if (latestEditInstance != null) {
            return InstanceEditResult.EditBlockedByNewerExistingEdit(latestEditInstance)
        }

        val formHash = Analytics.getParamValue("form")
        val actionValue = if (sourceInstance.status == com.jed.optima.forms.instances.Instance.STATUS_COMPLETE) {
            "finalized $formHash"
        } else {
            "sent $formHash"
        }
        Analytics.log(AnalyticsEvents.EDIT_FINALIZED_OR_SENT_FORM, "action", actionValue)

        val targetInstance = cloneInstance(sourceInstance, instanceFilePath, instancesDir, instancesRepository, formsRepository, clock)

        return InstanceEditResult.EditCompleted(targetInstance)
    }

    private fun findLatestEditIfExists(
        instance: com.jed.optima.forms.instances.Instance,
        instancesRepository: com.jed.optima.forms.instances.InstancesRepository
    ): com.jed.optima.forms.instances.Instance? {
        val editGroupId = if (instance.isEdit()) {
            instance.editOf
        } else {
            instance.dbId
        }

        return instancesRepository
            .all
            .filter { it.editOf == editGroupId }
            .maxByOrNull { it.editNumber!! }
            ?.takeIf { it.dbId != instance.dbId }
    }

    private fun cloneInstance(
        sourceInstance: com.jed.optima.forms.instances.Instance,
        instanceFilePath: String,
        instancesDir: String,
        instancesRepository: com.jed.optima.forms.instances.InstancesRepository,
        formsRepository: com.jed.optima.forms.FormsRepository,
        clock: () -> Long = { System.currentTimeMillis() }
    ): com.jed.optima.forms.instances.Instance {
        val formName = formsRepository.getAllByFormIdAndVersion(
            sourceInstance.formId,
            sourceInstance.formVersion
        ).first().displayName
        val targetInstanceFile = copyInstanceDir(File(instanceFilePath), instancesDir, formName, clock)

        return instancesRepository.save(
            com.jed.optima.forms.instances.Instance.Builder(sourceInstance)
                .dbId(null)
                .status(com.jed.optima.forms.instances.Instance.STATUS_NEW_EDIT)
                .instanceFilePath(targetInstanceFile.absolutePath)
                .editOf(sourceInstance.editOf ?: sourceInstance.dbId)
                .editNumber((sourceInstance.editNumber ?: 0) + 1)
                .build()
        )
    }

    private fun copyInstanceDir(
        sourceInstanceFile: File,
        instancesDir: String,
        formName: String,
        clock: () -> Long = { System.currentTimeMillis() }
    ): File {
        val sourceInstanceDir = sourceInstanceFile.parentFile!!
        val targetInstanceFile = createInstanceFile(formName, instancesDir, clock = clock)!!
        val targetInstanceDir = targetInstanceFile.parentFile!!

        sourceInstanceDir.copyRecursively(targetInstanceDir, true)
        File(targetInstanceDir, sourceInstanceFile.name).renameTo(targetInstanceFile)

        return targetInstanceFile
    }
}

sealed class InstanceEditResult(val instance: com.jed.optima.forms.instances.Instance) {
    data class EditCompleted(val resultInstance: com.jed.optima.forms.instances.Instance) : InstanceEditResult(resultInstance)
    data class EditBlockedByNewerExistingEdit(val resultInstance: com.jed.optima.forms.instances.Instance) : InstanceEditResult(resultInstance)
}
