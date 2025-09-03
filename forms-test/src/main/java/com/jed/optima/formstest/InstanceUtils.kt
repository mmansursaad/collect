package com.jed.optima.formstest

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InstanceUtils {

    @JvmStatic
    fun buildInstance(formId: String?, version: String?, instancesDir: String): _root_ide_package_.com.jed.optima.forms.instances.Instance.Builder {
        return buildInstance(
            formId,
            version,
            "display name",
            _root_ide_package_.com.jed.optima.forms.instances.Instance.STATUS_INCOMPLETE,
            null,
            instancesDir
        )
    }

    @JvmStatic
    fun buildInstance(
        formId: String?,
        version: String?,
        displayName: String?,
        status: String?,
        deletedDate: Long?,
        instancesDir: String
    ): _root_ide_package_.com.jed.optima.forms.instances.Instance.Builder {
        val instanceFile = createInstanceDirAndFile(instancesDir)

        return _root_ide_package_.com.jed.optima.forms.instances.Instance.Builder()
            .formId(formId)
            .formVersion(version)
            .displayName(displayName)
            .instanceFilePath(instanceFile.absolutePath)
            .status(status)
            .deletedDate(deletedDate)
    }

    @JvmStatic
    fun createInstanceDirAndFile(instancesDir: String): File {
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH)
            .format(Date((100_000_000_0000L..999_999_999_9999L).random()))

        val instanceDir = File(instancesDir + File.separator + _root_ide_package_.com.jed.optima.shared.strings.RandomString.randomString(5) + "_" + timestamp)
        instanceDir.mkdir()

        return createTempFile(instanceDir, instanceDir.name, ".xml").also {
            it.writeText(_root_ide_package_.com.jed.optima.shared.strings.RandomString.randomString(10))
        }
    }
}
