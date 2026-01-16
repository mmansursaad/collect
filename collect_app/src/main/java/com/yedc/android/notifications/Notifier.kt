package com.yedc.android.notifications

import com.yedc.android.formmanagement.ServerFormDetails
import com.yedc.android.formmanagement.download.FormDownloadException
import com.yedc.android.upload.FormUploadException
import com.yedc.forms.FormSourceException

interface Notifier {
    fun onUpdatesAvailable(updates: List<ServerFormDetails>, projectId: String)
    fun onUpdatesDownloaded(result: Map<ServerFormDetails, FormDownloadException?>, projectId: String)
    fun onSync(exception: FormSourceException?, projectId: String)
    fun onSubmission(result: Map<com.yedc.forms.instances.Instance, FormUploadException?>, projectId: String)
}
