package com.jed.optima.android.utilities

import android.content.Context
import com.jed.optima.android.formmanagement.ServerFormDetails
import com.jed.optima.android.formmanagement.download.FormDownloadException
import com.jed.optima.android.formmanagement.download.FormDownloadExceptionMapper
import com.jed.optima.errors.ErrorItem
import com.jed.optima.strings.localization.getLocalizedString

object FormsDownloadResultInterpreter {
    fun getFailures(result: Map<ServerFormDetails, FormDownloadException?>, context: Context) = result.filter {
        it.value != null
    }.map {
        ErrorItem(
            it.key.formName ?: "",
            context.getLocalizedString(com.jed.optima.strings.R.string.form_details, it.key.formId ?: "", it.key.formVersion ?: ""),
            FormDownloadExceptionMapper(context).getMessage(it.value)
        )
    }

    fun getNumberOfFailures(result: Map<ServerFormDetails, FormDownloadException?>) = result.count {
        it.value != null
    }

    fun allFormsDownloadedSuccessfully(result: Map<ServerFormDetails, FormDownloadException?>) = result.values.all {
        it == null
    }
}
