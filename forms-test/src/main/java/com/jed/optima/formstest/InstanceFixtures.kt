package com.jed.optima.formstest

import com.jed.optima.shared.TempFiles
import java.io.File

object InstanceFixtures {

    fun instance(
        status: String = _root_ide_package_.com.jed.optima.forms.instances.Instance.STATUS_INCOMPLETE,
        lastStatusChangeDate: Long = 0,
        displayName: String = "Form",
        dbId: Long? = null,
        form: _root_ide_package_.com.jed.optima.forms.Form? = null,
        deletedDate: Long? = null,
        canDeleteBeforeSend: Boolean = true,
        instancesDir: File = TempFiles.createTempDir(),
        formId: String = "formId",
        formVersion: String = "version",
        editOf: Long? = null,
        editNumber: Long? = null
    ): _root_ide_package_.com.jed.optima.forms.instances.Instance {
        return InstanceUtils.buildInstance(formId, formVersion, instancesDir.absolutePath)
            .status(status)
            .lastStatusChangeDate(lastStatusChangeDate)
            .displayName(displayName)
            .dbId(dbId).also {
                if (form != null) {
                    it.formId(form.formId)
                    it.formVersion(form.version)
                }
            }
            .deletedDate(deletedDate)
            .canDeleteBeforeSend(canDeleteBeforeSend)
            .editOf(editOf)
            .editNumber(editNumber)
            .build()
    }
}
