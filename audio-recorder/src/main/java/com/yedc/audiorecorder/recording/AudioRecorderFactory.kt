package com.yedc.audiorecorder.recording

import android.app.Application
import com.yedc.androidshared.data.getState
import com.yedc.audiorecorder.recording.internal.ForegroundServiceAudioRecorder
import com.yedc.audiorecorder.recording.internal.RecordingRepository

open class AudioRecorderFactory(private val application: Application) {

    open fun create(): AudioRecorder {
        return ForegroundServiceAudioRecorder(application, RecordingRepository(application.getState()))
    }
}
