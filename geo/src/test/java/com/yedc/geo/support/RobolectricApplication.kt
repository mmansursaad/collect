package com.yedc.geo.support

import android.app.Application
import com.yedc.androidshared.ui.Animations
import com.yedc.androidshared.ui.multiclicksafe.MultiClickGuard
import com.yedc.geo.GeoDependencyComponent
import com.yedc.geo.GeoDependencyComponentProvider

class RobolectricApplication : Application(), GeoDependencyComponentProvider {

    override lateinit var geoDependencyComponent: GeoDependencyComponent

    override fun onCreate() {
        super.onCreate()
        Animations.DISABLE_ANIMATIONS = true

        // We don't want any clicks to be blocked
        MultiClickGuard.test = true
    }
}
