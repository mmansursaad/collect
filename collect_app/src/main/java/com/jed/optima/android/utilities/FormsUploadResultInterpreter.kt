package com.jed.optima.android.utilities

import android.content.Context
import com.jed.optima.android.R
import com.jed.optima.android.instancemanagement.userVisibleInstanceName
import com.jed.optima.android.upload.FormUploadException
import com.jed.optima.errors.ErrorItem
import com.jed.optima.strings.localization.getLocalizedString

object FormsUploadResultInterpreter {
    fun getFailures(result: Map<com.jed.optima.forms.instances.Instance, FormUploadException?>, context: Context) = result.filter {
        it.value != null
    }.map {
        ErrorItem(
            it.key.userVisibleInstanceName(context.resources),
            context.getLocalizedString(com.jed.optima.strings.R.string.form_details, it.key.formId ?: "", it.key.formVersion ?: ""),
            it.value?.message ?: ""
        )
    }

    fun getNumberOfFailures(result: Map<com.jed.optima.forms.instances.Instance, FormUploadException?>) = result.count {
        it.value != null
    }

    fun allFormsUploadedSuccessfully(result: Map<com.jed.optima.forms.instances.Instance, FormUploadException?>) = result.values.all {
        it == null
    }
}
