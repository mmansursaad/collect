package com.yedc.android.notifications.builders

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import com.yedc.android.R
import com.yedc.android.formmanagement.FormSourceExceptionMapper
import com.yedc.android.notifications.NotificationManagerNotifier
import com.yedc.android.notifications.NotificationUtils
import com.yedc.android.notifications.NotificationUtils.createOpenErrorsActionIntent
import com.yedc.errors.ErrorItem
import com.yedc.forms.FormSourceException
import com.yedc.strings.localization.getLocalizedString

object FormsSyncFailedNotificationBuilder {

    fun build(application: Application, exception: FormSourceException, projectName: String, notificationId: Int): Notification {
        val contentIntent = NotificationUtils.createOpenAppContentIntent(application, notificationId)

        return NotificationCompat.Builder(
            application,
            NotificationManagerNotifier.COLLECT_NOTIFICATION_CHANNEL
        ).apply {
            setContentIntent(contentIntent)
            setContentTitle(application.getLocalizedString(com.yedc.strings.R.string.form_update_error))
            setSubText(projectName)
            setSmallIcon(com.yedc.icons.R.drawable.ic_notification_small_yedc)
            setAutoCancel(true)
            addAction(
                R.drawable.ic_outline_info_small,
                application.getLocalizedString(com.yedc.strings.R.string.show_details),
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
            application.getLocalizedString(com.yedc.strings.R.string.form_update_error),
            projectName,
            FormSourceExceptionMapper(application).getMessage(exception)
        )

        return createOpenErrorsActionIntent(application, listOf(errorItem), notificationId)
    }
}
