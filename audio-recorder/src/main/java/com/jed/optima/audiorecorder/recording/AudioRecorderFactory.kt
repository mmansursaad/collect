package com.jed.optima.audiorecorder.recording

import android.app.Application
import com.jed.optima.androidshared.data.getState
import com.jed.optima.audiorecorder.recording.internal.ForegroundServiceAudioRecorder
import com.jed.optima.audiorecorder.recording.internal.RecordingRepository

open class AudioRecorderFactory(private val application: Application) {

    open fun create(): AudioRecorder {
        return ForegroundServiceAudioRecorder(application, RecordingRepository(application.getState()))
    }
}
