package com.jed.optima.android.notifications.builders

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import com.jed.optima.android.R
import com.jed.optima.android.formmanagement.FormSourceExceptionMapper
import com.jed.optima.android.notifications.NotificationManagerNotifier
import com.jed.optima.android.notifications.NotificationUtils
import com.jed.optima.android.notifications.NotificationUtils.createOpenErrorsActionIntent
import com.jed.optima.errors.ErrorItem
import com.jed.optima.forms.FormSourceException
import com.jed.optima.strings.localization.getLocalizedString

object FormsSyncFailedNotificationBuilder {

    fun build(application: Application, exception: FormSourceException, projectName: String, notificationId: Int): Notification {
        val contentIntent = NotificationUtils.createOpenAppContentIntent(application, notificationId)

        return NotificationCompat.Builder(
            application,
            NotificationManagerNotifier.COLLECT_NOTIFICATION_CHANNEL
        ).apply {
            setContentIntent(contentIntent)
            setContentTitle(application.getLocalizedString(com.jed.optima.strings.R.string.form_update_error))
            setSubText(projectName)
            setSmallIcon(com.jed.optima.icons.R.drawable.ic_notification_small)
            setAutoCancel(true)
            addAction(
                R.drawable.ic_outline_info_small,
                application.getLocalizedString(com.jed.optima.strings.R.string.show_details),
                getShowDetailsPendingIntent(application, projectName, exception, notificationId)
            )
        }.build()
    }

    private fun getShowDetailsPendingIntent(
        application: Application,
        projectName: String,
        exception: FormSourceException,
        notificationId: Int
    ): PendingIntent {
        val errorItem = ErrorItem(
            application.getLocalizedString(com.jed.optima.strings.R.string.form_update_error),
            projectName,
            FormSourceExceptionMapper(application).getMessage(exception)
        )

        return createOpenErrorsActionIntent(application, listOf(errorItem), notificationId)
    }
}
