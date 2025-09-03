package com.jed.optima.android.widgets.utilities

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.widget.Toast
import org.javarosa.form.api.FormEntryPrompt
import com.jed.optima.android.R
import com.jed.optima.androidshared.system.IntentLauncher
import com.jed.optima.permissions.PermissionListener
import com.jed.optima.permissions.PermissionsProvider

class ExternalAppRecordingRequester(
    private val activity: Activity,
    private val intentLauncher: IntentLauncher,
    private val waitingForDataRegistry: com.jed.optima.android.widgets.utilities.WaitingForDataRegistry,
    private val permissionsProvider: PermissionsProvider
) : com.jed.optima.android.widgets.utilities.RecordingRequester {

    override fun requestRecording(prompt: FormEntryPrompt) {
        permissionsProvider.requestRecordAudioPermission(
            activity,
            object : PermissionListener {
                override fun granted() {
                    val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
                    intent.putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString()
                    )
                    waitingForDataRegistry.waitForData(prompt.index)
                    intentLauncher.launchForResult(
                        activity,
                        intent,
                        com.jed.optima.android.utilities.ApplicationConstants.RequestCodes.AUDIO_CAPTURE
                    ) {
                        Toast.makeText(
                            activity,
                            activity.getString(
                                com.jed.optima.strings.R.string.activity_not_found,
                                activity.getString(com.jed.optima.strings.R.string.capture_audio)
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                        waitingForDataRegistry.cancelWaitingForData()
                    }
                }
            }
        )
    }
}
