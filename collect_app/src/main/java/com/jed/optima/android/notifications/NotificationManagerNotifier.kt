package com.jed.optima.android.notifications

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.jed.optima.android.formmanagement.ServerFormDetails
import com.jed.optima.android.formmanagement.download.FormDownloadException
import com.jed.optima.android.notifications.builders.FormUpdatesAvailableNotificationBuilder
import com.jed.optima.android.notifications.builders.FormUpdatesDownloadedNotificationBuilder
import com.jed.optima.android.notifications.builders.FormsSubmissionNotificationBuilder
import com.jed.optima.android.notifications.builders.FormsSyncFailedNotificationBuilder
import com.jed.optima.android.upload.FormUploadException
import com.jed.optima.forms.FormSourceException
import com.jed.optima.projects.ProjectsRepository
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.settings.keys.MetaKeys
import com.jed.optima.strings.localization.getLocalizedString

class NotificationManagerNotifier(
    private val application: Application,
    private val settingsProvider: SettingsProvider,
    private val projectsRepository: ProjectsRepository
) : Notifier {
    private val notificationManager: NotificationManager =
        application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun onUpdatesAvailable(updates: List<ServerFormDetails>, projectId: String) {
        val metaPrefs = settingsProvider.getMetaSettings()
        val updateId = updates
            .mapTo(HashSet()) { (_, _, formId, _, hash, _, _, manifest) -> formId + hash + manifest?.hash }
        if (metaPrefs.getStringSet(MetaKeys.LAST_UPDATED_NOTIFICATION) != updateId) {
            notificationManager.notify(
                FORM_UPDATE_NOTIFICATION_ID,
                FormUpdatesAvailableNotificationBuilder.build(
                    application,
                    getProjectName(projectId),
                    FORM_UPDATE_NOTIFICATION_ID
                )
            )
            metaPrefs.save(MetaKeys.LAST_UPDATED_NOTIFICATION, updateId)
        }
    }

    override fun onUpdatesDownloaded(result: Map<ServerFormDetails, FormDownloadException?>, projectId: String) {
        notificationManager.notify(
            FORM_UPDATE_NOTIFICATION_ID,
            FormUpdatesDownloadedNotificationBuilder.build(
                application,
                result,
                getProjectName(projectId),
                FORM_UPDATE_NOTIFICATION_ID
            )
        )
    }

    override fun onSync(exception: FormSourceException?, projectId: String) {
        if (exception == null) {
            notificationManager.cancel(FORM_SYNC_NOTIFICATION_ID)
        } else {
            notificationManager.notify(
                FORM_SYNC_NOTIFICATION_ID,
                FormsSyncFailedNotificationBuilder.build(
                    application,
                    exception,
                    getProjectName(projectId),
                    FORM_SYNC_NOTIFICATION_ID
                )
            )
        }
    }

    override fun onSubmission(result: Map<com.jed.optima.forms.instances.Instance, FormUploadException?>, projectId: String) {
        notificationManager.notify(
            AUTO_SEND_RESULT_NOTIFICATION_ID,
            FormsSubmissionNotificationBuilder.build(
                application,
                result,
                getProjectName(projectId),
                AUTO_SEND_RESULT_NOTIFICATION_ID
            )
        )
    }

    companion object {
        const val COLLECT_NOTIFICATION_CHANNEL = "collect_notification_channel"
        const val FORM_UPDATE_NOTIFICATION_ID = 0
        const val FORM_SYNC_NOTIFICATION_ID = 1
        private const val AUTO_SEND_RESULT_NOTIFICATION_ID = 1328974928
    }

    private fun getProjectName(projectId: String) = projectsRepository.get(projectId)?.name ?: ""

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    COLLECT_NOTIFICATION_CHANNEL,
                    application.getLocalizedString(com.jed.optima.strings.R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
    }
}
