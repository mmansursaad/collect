package com.jed.optima.audiorecorder.recording.internal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import com.jed.optima.androidshared.ui.ReturnToAppActivity
import com.jed.optima.audiorecorder.R
import com.jed.optima.audiorecorder.recording.RecordingSession
import com.jed.optima.strings.format.formatLength
import com.jed.optima.strings.localization.getLocalizedString

internal class RecordingForegroundServiceNotification(private val service: Service, private val recordingRepository: RecordingRepository) {

    private val notificationIntent = Intent(service, ReturnToAppActivity::class.java)
    private val notificationBuilder = NotificationCompat.Builder(service, NOTIFICATION_CHANNEL)
        .setContentTitle(service.getLocalizedString(com.jed.optima.strings.R.string.recording))
        .setContentText(formatLength(0))
        .setSmallIcon(com.jed.optima.icons.R.drawable.ic_stat_jed_logo)
        .setContentIntent(PendingIntent.getActivity(service, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE))
        .setPriority(NotificationCompat.PRIORITY_LOW)

    private val notificationManager = (service.getSystemService(NOTIFICATION_SERVICE) as NotificationManager)

    private val sessionObserver = Observer<RecordingSession?> {
        if (it != null) {
            notificationBuilder.setContentText(formatLength(it.duration))
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    fun show() {
        setupNotificationChannel()
        val notification = notificationBuilder
            .build()

        service.startForeground(NOTIFICATION_ID, notification)
        recordingRepository.currentSession.observeForever(sessionObserver)
    }

    fun dismiss() {
        recordingRepository.currentSession.removeObserver(sessionObserver)
        service.stopSelf()
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL,
                service.getLocalizedString(com.jed.optima.strings.R.string.recording_channel),
                NotificationManager.IMPORTANCE_LOW
            )

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL = "recording_channel"
    }
}
