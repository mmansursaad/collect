package com.jed.optima.android.notifications.builders

import android.app.Application
import android.app.Notification
import androidx.core.app.NotificationCompat
import com.jed.optima.android.notifications.NotificationManagerNotifier
import com.jed.optima.android.notifications.NotificationUtils
import com.jed.optima.strings.localization.getLocalizedString

object FormUpdatesAvailableNotificationBuilder {

    @JvmStatic
    fun build(application: Application, projectName: String, notificationId: Int): Notification {
        val contentIntent = NotificationUtils.createOpenAppContentIntent(
            application,
            notificationId
        )

        return NotificationCompat.Builder(
            application,
            NotificationManagerNotifier.COLLECT_NOTIFICATION_CHANNEL
        ).apply {
            setContentIntent(contentIntent)
            setContentTitle(application.getLocalizedString(com.jed.optima.strings.R.string.form_updates_available))
            setContentText(null)
            setSubText(projectName)
            setSmallIcon(com.jed.optima.icons.R.drawable.ic_notification_small)
            setAutoCancel(true)
        }.build()
    }
}
