package com.yedc.android.formmanagement

import android.content.Context
import com.yedc.android.R
import com.yedc.forms.FormSourceException
import com.yedc.strings.localization.getLocalizedString

class FormSourceExceptionMapper(private val context: Context) {
    fun getMessage(exception: FormSourceException?): String {
        return when (exception) {
            is FormSourceException.Unreachable -> {
                context.getLocalizedString(
                    com.yedc.strings.R.string.unreachable_error,
                    exception.serverUrl
                ) + " " + context.getLocalizedString(
                    com.yedc.strings.R.string.report_to_project_lead
                )
            }
            is FormSourceException.SecurityError -> {
                context.getLocalizedString(
                    com.yedc.strings.R.string.security_error,
                    exception.serverUrl
                ) + " " + context.getLocalizedString(
                    com.yedc.strings.R.string.report_to_project_lead
                )
            }
            is FormSourceException.ServerError -> {
                context.getLocalizedString(
                    com.yedc.strings.R.string.server_error,
                    exception.serverUrl,
                    exception.statusCode
                ) + " " + context.getLocalizedString(
                    com.yedc.strings.R.string.report_to_project_lead
                )
            }
            is FormSourceException.ParseError -> {
                context.getLocalizedString(
                    com.yedc.strings.R.string.invalid_response,
                    exception.serverUrl
                ) + " " + context.getLocalizedString(
                    com.yedc.strings.R.string.report_to_project_lead
                )
            }
            is FormSourceException.ServerNotOpenRosaError -> {
                "This server does not correctly implement the OpenRosa formList API." + " " + context.getLocalizedString(
                    com.yedc.strings.R.string.report_to_project_lead
                )
            }
            else -> {
                context.getLocalizedString(com.yedc.strings.R.string.report_to_project_lead)
            }
        }
    }
}
