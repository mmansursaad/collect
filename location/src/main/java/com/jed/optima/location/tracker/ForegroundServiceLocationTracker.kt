package com.jed.optima.location.tracker

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.jed.optima.androidshared.data.getState
import com.jed.optima.androidshared.ui.ReturnToAppActivity
import com.jed.optima.location.Location
import com.jed.optima.location.LocationClientProvider
import com.jed.optima.location.R
import com.jed.optima.strings.localization.getLocalizedString

private const val LOCATION_KEY = "location"

class ForegroundServiceLocationTracker(private val application: Application) : LocationTracker {

    override fun getCurrentLocation(): Location? {
        return application.getState().get(LOCATION_KEY)
    }

    override fun start(retainMockAccuracy: Boolean, updateInterval: Long?) {
        val intent = Intent(application, LocationTrackerService::class.java).also { intent ->
            intent.putExtra(LocationTrackerService.EXTRA_RETAIN_MOCK_ACCURACY, retainMockAccuracy)
            updateInterval?.let {
                intent.putExtra(LocationTrackerService.EXTRA_UPDATE_INTERVAL, it)
            }
        }

        application.startService(intent)
    }

    override fun stop() {
        application.stopService(Intent(application, LocationTrackerService::class.java))
    }
}

class LocationTrackerService : Service(), _root_ide_package_.com.jed.optima.location.LocationClient.LocationClientListener {

    private val locationClient: _root_ide_package_.com.jed.optima.location.LocationClient by lazy {
        LocationClientProvider.getClient(application)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setupNotificationChannel()
        startForeground(
            NOTIFICATION_ID,
            createNotification()
        )

        locationClient.setRetainMockAccuracy(
            intent?.getBooleanExtra(
                EXTRA_RETAIN_MOCK_ACCURACY,
                false
            ) ?: false
        )

        if (intent?.hasExtra(EXTRA_UPDATE_INTERVAL) == true) {
            val interval = intent.getLongExtra(EXTRA_UPDATE_INTERVAL, -1)
            locationClient.setUpdateIntervals(
                interval,
                interval / 2
            )
        }

        locationClient.start(this)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        locationClient.stop()
        application.getState().clear(LOCATION_KEY)
    }

    override fun onClientStart() {
        locationClient.requestLocationUpdates {
            application.getState().set(
                LOCATION_KEY,
                Location(it.latitude, it.longitude, it.altitude, it.accuracy)
            )
        }
    }

    override fun onClientStartFailure() {
        // Ignored
    }

    override fun onClientStop() {
        // Ignored
    }

    private fun createNotification(): Notification {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
            .setSmallIcon(com.jed.optima.icons.R.drawable.ic_stat_jed_logo)
            .setContentTitle(getLocalizedString(com.jed.optima.strings.R.string.location_tracking_notification_title))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(createNotificationIntent())

        return notification
            .build()
    }

    private fun createNotificationIntent() =
        PendingIntent.getActivity(this, 0, Intent(this, ReturnToAppActivity::class.java), PendingIntent.FLAG_IMMUTABLE)

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL,
                getLocalizedString(com.jed.optima.strings.R.string.location_tracking_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )

            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                notificationChannel
            )
        }
    }

    companion object {
        const val EXTRA_RETAIN_MOCK_ACCURACY = "retain_mock_accuracy"
        const val EXTRA_UPDATE_INTERVAL = "update_interval"

        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL = "location_tracking"
    }
}
