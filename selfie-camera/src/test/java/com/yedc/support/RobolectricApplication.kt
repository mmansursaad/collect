package com.yedc.selfiecamera.support

import android.app.Application
import com.yedc.androidshared.ui.ToastUtils
import com.yedc.selfiecamera.SelfieCameraDependencyComponent
import com.yedc.selfiecamera.SelfieCameraDependencyComponentProvider

class RobolectricApplication : Application(), SelfieCameraDependencyComponentProvider {

    override lateinit var selfieCameraDependencyComponent: SelfieCameraDependencyComponent

    override fun onCreate() {
        super.onCreate()
        ToastUtils.setApplication(this)
    }
}
