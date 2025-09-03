package com.jed.optima.android.widgets.utilities

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import org.javarosa.form.api.FormEntryPrompt
import com.jed.optima.android.R
import com.jed.optima.androidshared.system.IntentLauncher

class GetContentAudioFileRequester(
    private val activity: Activity,
    private val intentLauncher: IntentLauncher,
    private val waitingForDataRegistry: com.jed.optima.android.widgets.utilities.WaitingForDataRegistry
) : com.jed.optima.android.widgets.utilities.AudioFileRequester {

    override fun requestFile(prompt: FormEntryPrompt) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/*"
        waitingForDataRegistry.waitForData(prompt.index)
        intentLauncher.launchForResult(
            activity,
            intent,
            com.jed.optima.android.utilities.ApplicationConstants.RequestCodes.AUDIO_CHOOSER
        ) {
            Toast.makeText(
                activity,
                activity.getString(
                    com.jed.optima.strings.R.string.activity_not_found,
                    activity.getString(com.jed.optima.strings.R.string.choose_sound)
                ),
                Toast.LENGTH_SHORT
            ).show()
            waitingForDataRegistry.cancelWaitingForData()
        }
    }
}
