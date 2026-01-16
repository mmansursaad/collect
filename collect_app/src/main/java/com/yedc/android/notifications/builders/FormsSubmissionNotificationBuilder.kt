package com.yedc.android.notifications.builders

import android.app.Application
import android.app.Notification
import androidx.core.app.NotificationCompat
import com.yedc.android.R
import com.yedc.android.notifications.NotificationManagerNotifier
import com.yedc.android.notifications.NotificationUtils
import com.yedc.android.upload.FormUploadException
import com.yedc.android.utilities.FormsUploadResultInterpreter
import com.yedc.strings.localization.getLocalizedString

object FormsSubmissionNotificationBuilder {

    fun build(
        application: Application,
        result: Map<com.yedc.forms.instances.Instance, FormUploadException?>,
        projectName: String,
        notificationId: Int
    ): Notification {
        val allFormsUploadedSuccessfully = FormsUploadResultInterpreter.allFormsUploadedSuccessfully(result)

        return NotificationCompat.Builder(
            application,
            NotificationManagerNotifier.COLLECT_NOTIFICATION_CHANNEL
        ).apply {
            setContentIntent(
                NotificationUtils.createOpenAppContentIntent(
                    application,
                    notificationId
                )
            )
            setContentTitle(getTitle(application, allFormsUploadedSuccessfully))
            setContentText(getMessage(application, allFormsUploadedSuccessfully, result))
            setSubText(projectName)
            setSmallIcon(com.yedc.icons.R.drawable.ic_notification_small_yedc)
            setAutoCancel(true)

            if (!allFormsUploadedSuccessfully) {
                val errorItems = FormsUploadResultInterpreter.getFailures(result, application)

                addAction(
                    R.drawable.ic_outline_info_small,
                    application.getLocalizedString(com.yedc.strings.R.string.show_details),
                    NotificationUtils.createOpenErrorsActionIntent(application, errorItems, notificationId)
                )
            }
        }.build()
    }

    private fun getTitle(application: Application, allFormsUploadedSuccessfully: Boolean): String {
        return if (allFormsUploadedSuccessfully) {
            application.getLocalizedString(com.yedc.strings.R.string.forms_upload_succeeded)
        } else {
            application.getLocalizedString(com.yedc.strings.R.string.forms_upload_failed)
        }
    }

    private fun getMessage(application: Application, allFormsUploadedSuccessfully: Boolean, result: Map<com.yedc.forms.instances.Instance, FormUploadException?>): String {
        return if (allFormsUploadedSuccessfully) {
            application.getLocalizedString(com.yedc.strings.R.string.all_uploads_succeeded)
        } else {
            application.getLocalizedString(
                com.yedc.strings.R.string.some_uploads_failed,
                FormsUploadResultInterpreter.getNumberOfFailures(result),
                result.size
            )
        }
    }
}
