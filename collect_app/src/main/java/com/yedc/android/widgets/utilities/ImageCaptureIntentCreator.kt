package com.yedc.android.widgets.utilities

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import org.javarosa.form.api.FormEntryPrompt
import com.yedc.android.BuildConfig
import timber.log.Timber
import java.io.File

object ImageCaptureIntentCreator {
    @JvmStatic
    fun imageCaptureIntent(prompt: FormEntryPrompt, context: Context, tmpImageFilePath: String): Intent {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val packageName = _root_ide_package_.com.yedc.android.utilities.FormEntryPromptUtils.getAdditionalAttribute(prompt, "intent")
        if (packageName != null) {
            intent.setPackage(packageName)
        }

        // The Android Camera application saves a full-size photo if you give it a file to save into.
        // You must provide a fully qualified file name where the camera app should save the photo.
        // https://developer.android.com/training/camera-deprecated/photobasics
        try {
            val uri = _root_ide_package_.com.yedc.android.utilities.ContentUriProvider().getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".provider",
                File(tmpImageFilePath)
            )
            // if this gets modified, the onActivityResult in
            // FormEntyActivity will also need to be updated.
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            _root_ide_package_.com.yedc.android.utilities.FileUtils.grantFilePermissions(intent, uri, context)
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
        }

        return intent
    }
}
