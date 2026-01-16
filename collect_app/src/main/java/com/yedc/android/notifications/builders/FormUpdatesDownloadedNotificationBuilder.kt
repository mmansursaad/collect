package com.yedc.android.notifications.builders

import android.app.Application
import android.app.Notification
import androidx.core.app.NotificationCompat
import com.yedc.android.R
import com.yedc.android.formmanagement.ServerFormDetails
import com.yedc.android.formmanagement.download.FormDownloadException
import com.yedc.android.notifications.NotificationManagerNotifier
import com.yedc.android.notifications.NotificationUtils
import com.yedc.android.utilities.FormsDownloadResultInterpreter
import com.yedc.strings.localization.getLocalizedString

object FormUpdatesDownloadedNotificationBuilder {

    fun build(application: Application, result: Map<ServerFormDetails, FormDownloadException?>, projectName: String, notificationId: Int): Notification {
        val allFormsDownloadedSuccessfully = FormsDownloadResultInterpreter.allFormsDownloadedSuccessfully(result)

        val contentIntent = NotificationUtils.createOpenAppContentIntent(
            application,
            notificationId
        )

        val errorItems = FormsDownloadResultInterpreter.getFailures(result, application)
        val showDetailsIntent =
            NotificationUtils.createOpenErrorsActionIntent(application, errorItems, notificationId)

        val title =
            if (allFormsDownloadedSuccessfully) {
                application.getLocalizedString(com.yedc.strings.R.string.forms_download_succeeded)
            } else {
                application.getLocalizedString(com.yedc.strings.R.string.forms_download_failed)
            }

        val message =
            if (allFormsDownloadedSuccessfully) {
                application.getLocalizedString(com.yedc.strings.R.string.all_downloads_succeeded)
            } else {
                application.getLocalizedString(
                    com.yedc.strings.R.string.some_downloads_failed,
                    FormsDownloadResultInterpreter.getNumberOfFailures(result),
                    result.size
                )
            }

        return NotificationCompat.Builder(
            application,
            NotificationManagerNotifier.COLLECT_NOTIFICATION_CHANNEL
        ).apply {
            setContentIntent(contentIntent)
            setContentTitle(title)
            setContentText(message)
            setSubText(projectName)
            setSmallIcon(com.yedc.icons.R.drawable.ic_notification_small_yedc)
            setAutoCancel(true)

            if (!allFormsDownloadedSuccessfully) {
                addAction(
                    R.drawable.ic_outline_info_small,
                    application.getLocalizedString(com.yedc.strings.R.string.show_details),
                    showDetailsIntent
                )
            }
        }.build()
    }
}
