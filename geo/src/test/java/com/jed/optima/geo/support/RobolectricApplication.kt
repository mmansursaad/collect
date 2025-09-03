package com.jed.optima.geo.support

import android.app.Application
import com.jed.optima.androidshared.ui.Animations
import com.jed.optima.androidshared.ui.multiclicksafe.MultiClickGuard
import com.jed.optima.geo.GeoDependencyComponent
import com.jed.optima.geo.GeoDependencyComponentProvider

class RobolectricApplication : Application(), GeoDependencyComponentProvider {

    override lateinit var geoDependencyComponent: GeoDependencyComponent

    override fun onCreate() {
        super.onCreate()
        Animations.DISABLE_ANIMATIONS = true

        // We don't want any clicks to be blocked
        MultiClickGuard.test = true
    }
}
