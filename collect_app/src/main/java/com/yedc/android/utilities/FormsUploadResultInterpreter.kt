package com.yedc.android.utilities

import android.content.Context
import com.yedc.android.R
import com.yedc.android.instancemanagement.userVisibleInstanceName
import com.yedc.android.upload.FormUploadException
import com.yedc.errors.ErrorItem
import com.yedc.strings.localization.getLocalizedString

object FormsUploadResultInterpreter {
    fun getFailures(result: Map<com.yedc.forms.instances.Instance, FormUploadException?>, context: Context) = result.filter {
        it.value != null
    }.map {
        ErrorItem(
            it.key.userVisibleInstanceName(context.resources),
            context.getLocalizedString(com.yedc.strings.R.string.form_details, it.key.formId ?: "", it.key.formVersion ?: ""),
            it.value?.message ?: ""
        )
    }

    fun getNumberOfFailures(result: Map<com.yedc.forms.instances.Instance, FormUploadException?>) = result.count {
        it.value != null
    }

    fun allFormsUploadedSuccessfully(result: Map<com.yedc.forms.instances.Instance, FormUploadException?>) = result.values.all {
        it == null
    }
}
