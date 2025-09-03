package com.jed.optima.android.notifications

import com.jed.optima.android.formmanagement.ServerFormDetails
import com.jed.optima.android.formmanagement.download.FormDownloadException
import com.jed.optima.android.upload.FormUploadException
import com.jed.optima.forms.FormSourceException

interface Notifier {
    fun onUpdatesAvailable(updates: List<ServerFormDetails>, projectId: String)
    fun onUpdatesDownloaded(result: Map<ServerFormDetails, FormDownloadException?>, projectId: String)
    fun onSync(exception: FormSourceException?, projectId: String)
    fun onSubmission(result: Map<com.jed.optima.forms.instances.Instance, FormUploadException?>, projectId: String)
}
