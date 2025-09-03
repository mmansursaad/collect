package com.jed.optima.formstest

import com.jed.optima.forms.MediaFile
import com.jed.optima.shared.TempFiles
import java.io.File
import java.util.UUID

object FormFixtures {
    // If you set the date here, it might be overridden with the current date during saving to the database if dbId is not set too
    fun form(
        formId: String = "formId",
        version: String = "1",
        formFilePath: String? = null,
        mediaFiles: List<Pair<String, String>> = emptyList(),
        autoSend: String? = null
    ): _root_ide_package_.com.jed.optima.forms.Form {
        val formFilesPath = TempFiles.createTempDir().absolutePath
        val mediaFilePath = TempFiles.createTempDir().absolutePath

        mediaFiles.forEach { (name, contents) ->
            File(mediaFilePath, name).also { it.writeBytes(contents.toByteArray()) }
        }

        return _root_ide_package_.com.jed.optima.forms.Form.Builder()
            .displayName("Test Form")
            .formId(formId)
            .version(version)
            .formFilePath(
                formFilePath ?: FormUtils.createFormFixtureFile(
                    formId,
                    version,
                    formFilesPath
                )
            )
            .formMediaPath(mediaFilePath)
            .autoSend(autoSend)
            .build()
    }

    fun mediaFile(
        integrityUrl: String? = null,
        hash: String = UUID.randomUUID().toString()
    ): MediaFile {
        return MediaFile(
            "file",
            hash,
            "http://example.com/download",
            type = null,
            integrityUrl
        )
    }
}
