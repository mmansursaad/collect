package com.jed.optima.android.formmanagement

import android.content.Context
import com.jed.optima.android.R
import com.jed.optima.forms.FormSourceException
import com.jed.optima.strings.localization.getLocalizedString

class FormSourceExceptionMapper(private val context: Context) {
    fun getMessage(exception: FormSourceException?): String {
        return when (exception) {
            is FormSourceException.Unreachable -> {
                context.getLocalizedString(
                    com.jed.optima.strings.R.string.unreachable_error,
                    exception.serverUrl
                ) + " " + context.getLocalizedString(
                    com.jed.optima.strings.R.string.report_to_project_lead
                )
            }
            is FormSourceException.SecurityError -> {
                context.getLocalizedString(
                    com.jed.optima.strings.R.string.security_error,
                    exception.serverUrl
                ) + " " + context.getLocalizedString(
                    com.jed.optima.strings.R.string.report_to_project_lead
                )
            }
            is FormSourceException.ServerError -> {
                context.getLocalizedString(
                    com.jed.optima.strings.R.string.server_error,
                    exception.serverUrl,
                    exception.statusCode
                ) + " " + context.getLocalizedString(
                    com.jed.optima.strings.R.string.report_to_project_lead
                )
            }
            is FormSourceException.ParseError -> {
                context.getLocalizedString(
                    com.jed.optima.strings.R.string.invalid_response,
                    exception.serverUrl
                ) + " " + context.getLocalizedString(
                    com.jed.optima.strings.R.string.report_to_project_lead
                )
            }
            is FormSourceException.ServerNotOpenRosaError -> {
                "This server does not correctly implement the OpenRosa formList API." + " " + context.getLocalizedString(
                    com.jed.optima.strings.R.string.report_to_project_lead
                )
            }
            else -> {
                context.getLocalizedString(com.jed.optima.strings.R.string.report_to_project_lead)
            }
        }
    }
}
