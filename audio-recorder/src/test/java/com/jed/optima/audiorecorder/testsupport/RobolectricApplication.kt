package com.jed.optima.audiorecorder.testsupport

import android.app.Application
import com.jed.optima.androidshared.data.AppState
import com.jed.optima.androidshared.data.StateStore
import com.jed.optima.audiorecorder.AudioRecorderDependencyComponent
import com.jed.optima.audiorecorder.AudioRecorderDependencyComponentProvider
import com.jed.optima.audiorecorder.AudioRecorderDependencyModule
import com.jed.optima.audiorecorder.DaggerAudioRecorderDependencyComponent

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
