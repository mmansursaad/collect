package com.yedc.android.notifications.builders

import android.app.Application
import android.app.Notification
import androidx.core.app.NotificationCompat
import com.yedc.android.notifications.NotificationManagerNotifier
import com.yedc.android.notifications.NotificationUtils
import com.yedc.strings.localization.getLocalizedString

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
            setContentTitle(application.getLocalizedString(com.yedc.strings.R.string.form_updates_available))
            setContentText(null)
            setSubText(projectName)
            setSmallIcon(com.yedc.icons.R.drawable.ic_notification_small_yedc)
            setAutoCancel(true)
        }.build()
    }
}
