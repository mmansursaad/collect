package com.jed.optima.audiorecorder.recorder

import com.jed.optima.audiorecorder.recording.MicInUseException
import com.jed.optima.audiorecorder.recording.SetupException
import java.io.File

interface Recorder {

    @Throws(SetupException::class, MicInUseException::class)
    fun start(output: Output)
    fun pause()
    fun resume()
    fun stop(): File
    fun cancel()

    val amplitude: Int
    fun isRecording(): Boolean
}

enum class Output {
    AMR,
    AAC,
    AAC_LOW
}
