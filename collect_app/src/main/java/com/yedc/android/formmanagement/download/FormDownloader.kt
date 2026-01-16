package com.yedc.android.formmanagement.download

import com.yedc.android.formmanagement.ServerFormDetails
import java.util.function.Supplier

interface FormDownloader {

    @Throws(FormDownloadException::class)
    fun downloadForm(
        form: ServerFormDetails?,
        progressReporter: ProgressReporter?,
        isCancelled: Supplier<Boolean?>?
    )

    interface ProgressReporter {
        fun onDownloadingMediaFile(count: Int)
    }
}
