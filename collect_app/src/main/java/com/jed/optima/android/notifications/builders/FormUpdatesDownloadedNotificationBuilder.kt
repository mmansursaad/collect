package com.jed.optima.android.notifications.builders

import android.app.Application
import android.app.Notification
import androidx.core.app.NotificationCompat
import com.jed.optima.android.R
import com.jed.optima.android.formmanagement.ServerFormDetails
import com.jed.optima.android.formmanagement.download.FormDownloadException
import com.jed.optima.android.notifications.NotificationManagerNotifier
import com.jed.optima.android.notifications.NotificationUtils
import com.jed.optima.android.utilities.FormsDownloadResultInterpreter
import com.jed.optima.strings.localization.getLocalizedString

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
                application.getLocalizedString(com.jed.optima.strings.R.string.forms_download_succeeded)
            } else {
                application.getLocalizedString(com.jed.optima.strings.R.string.forms_download_failed)
            }

        val message =
            if (allFormsDownloadedSuccessfully) {
                application.getLocalizedString(com.jed.optima.strings.R.string.all_downloads_succeeded)
            } else {
                application.getLocalizedString(
                    com.jed.optima.strings.R.string.some_downloads_failed,
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
            setSmallIcon(com.jed.optima.icons.R.drawable.ic_stat_jed_logo)
            setAutoCancel(true)

            if (!allFormsDownloadedSuccessfully) {
                addAction(
                    R.drawable.ic_outline_info_small,
                    application.getLocalizedString(com.jed.optima.strings.R.string.show_details),
                    showDetailsIntent
                )
            }
        }.build()
    }
}
