package com.jed.optima.android.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.jed.optima.android.BuildConfig
import com.jed.optima.android.mainmenu.MainMenuActivity
import com.jed.optima.errors.ErrorActivity
import com.jed.optima.errors.ErrorItem
import java.io.Serializable

object NotificationUtils {

    /**
     * Creates a [PendingIntent] that will start the [MainMenuActivity]. [MainMenuActivity]
     * finishes automatically if it's not started as the root of a task, so that means the
     * [Intent] will either land the user where they were last or reopen the app.
     */
    fun createOpenAppContentIntent(context: Context, notificationId: Int): PendingIntent {
        val intent = context
            .packageManager
            .getLaunchIntentForPackage(BuildConfig.APPLICATION_ID)

        return PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun createOpenErrorsActionIntent(
        context: Context,
        errorItems: List<ErrorItem>,
        notificationId: Int
    ): PendingIntent {
        val showDetailsIntent = Intent(context, ErrorActivity::class.java).apply {
            putExtra(ErrorActivity.EXTRA_ERRORS, errorItems as Serializable)
            putExtra(ErrorActivity.EXTRA_NOTIFICATION_ID, notificationId)
        }

        return PendingIntent.getActivity(
            context,
            notificationId,
            showDetailsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
