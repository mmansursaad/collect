package com.jed.optima.location

import android.app.Application
import com.jed.optima.androidshared.data.AppState
import com.jed.optima.androidshared.data.StateStore

class RobolectricApplication : Application(), StateStore {

    private val appState = AppState()

    override fun getState(): AppState {
        return appState
    }
}
