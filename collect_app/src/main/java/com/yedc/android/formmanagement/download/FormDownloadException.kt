package com.yedc.android.formmanagement.download

import com.yedc.forms.FormSourceException

sealed class FormDownloadException : Exception() {
    class DownloadingInterrupted : FormDownloadException()
    class FormWithNoHash : FormDownloadException()
    class FormParsingError(val original: Exception) : FormDownloadException()
    class DiskError : FormDownloadException()
    class InvalidSubmission : FormDownloadException()
    class FormSourceError(val exception: FormSourceException) : FormDownloadException()
}
