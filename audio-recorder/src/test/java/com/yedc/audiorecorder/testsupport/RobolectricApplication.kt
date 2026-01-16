package com.yedc.audiorecorder.testsupport

import android.app.Application
import com.yedc.androidshared.data.AppState
import com.yedc.androidshared.data.StateStore
import com.yedc.audiorecorder.AudioRecorderDependencyComponent
import com.yedc.audiorecorder.AudioRecorderDependencyComponentProvider
import com.yedc.audiorecorder.AudioRecorderDependencyModule
import com.yedc.audiorecorder.DaggerAudioRecorderDependencyComponent

/**
 * Used as the Application in tests in in the `test/src` root. This is setup in `robolectric.properties`
 */
internal class RobolectricApplication : Application(), AudioRecorderDependencyComponentProvider, StateStore {

    override lateinit var audioRecorderDependencyComponent: AudioRecorderDependencyComponent

    private val appState = AppState()

    override fun onCreate() {
        super.onCreate()
        audioRecorderDependencyComponent = DaggerAudioRecorderDependencyComponent.builder()
            .application(this)
            .build()
    }

    fun setupDependencies(dependencyModule: AudioRecorderDependencyModule) {
        audioRecorderDependencyComponent = DaggerAudioRecorderDependencyComponent.builder()
            .dependencyModule(dependencyModule)
            .application(this)
            .build()
    }

    override fun getState(): AppState {
        return appState
    }
}
