package com.jed.optima.lists

import android.app.Application
import com.jed.optima.androidshared.ui.multiclicksafe.MultiClickGuard

class RobolectricApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // We don't want any clicks to be blocked
        MultiClickGuard.test = true
    }
}
