package com.jed.optima.selfiecamera.support

import android.app.Application
import com.jed.optima.androidshared.ui.ToastUtils
import com.jed.optima.selfiecamera.SelfieCameraDependencyComponent
import com.jed.optima.selfiecamera.SelfieCameraDependencyComponentProvider

class RobolectricApplication : Application(), SelfieCameraDependencyComponentProvider {

    override lateinit var selfieCameraDependencyComponent: SelfieCameraDependencyComponent

    override fun onCreate() {
        super.onCreate()
        ToastUtils.setApplication(this)
    }
}
