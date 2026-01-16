package com.yedc.location

import android.app.Application
import com.yedc.androidshared.data.AppState
import com.yedc.androidshared.data.StateStore

class RobolectricApplication : Application(), StateStore {

    private val appState = AppState()

    override fun getState(): AppState {
        return appState
    }
}
