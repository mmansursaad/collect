package com.yedc.android.widgets.utilities

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import org.javarosa.form.api.FormEntryPrompt
import com.yedc.android.R
import com.yedc.androidshared.system.IntentLauncher

class GetContentAudioFileRequester(
    private val activity: Activity,
    private val intentLauncher: IntentLauncher,
    private val waitingForDataRegistry: _root_ide_package_.com.yedc.android.widgets.utilities.WaitingForDataRegistry
) : _root_ide_package_.com.yedc.android.widgets.utilities.AudioFileRequester {

    override fun requestFile(prompt: FormEntryPrompt) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/*"
        waitingForDataRegistry.waitForData(prompt.index)
        intentLauncher.launchForResult(
            activity,
            intent,
            _root_ide_package_.com.yedc.android.utilities.ApplicationConstants.RequestCodes.AUDIO_CHOOSER
        ) {
            Toast.makeText(
                activity,
                activity.getString(
                    com.yedc.strings.R.string.activity_not_found,
                    activity.getString(com.yedc.strings.R.string.choose_sound)
                ),
                Toast.LENGTH_SHORT
            ).show()
            waitingForDataRegistry.cancelWaitingForData()
        }
    }
}
