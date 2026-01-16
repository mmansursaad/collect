package com.yedc.android.configure.qr

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import com.yedc.android.preferences.SettingsExt.getExperimentalOptIn
import com.yedc.qrcode.BarcodeScannerView
import com.yedc.qrcode.BarcodeScannerViewContainer
import com.yedc.qrcode.mlkit.PlayServicesFallbackBarcodeScannerViewFactory
import com.yedc.qrcode.zxing.ZxingBarcodeScannerViewFactory
import com.yedc.settings.keys.ProjectKeys
import com.yedc.shared.settings.Settings

class SettingsBarcodeScannerViewFactory(
    private val settings: Settings
) : BarcodeScannerViewContainer.Factory {
    private val playServicesFallbackFactory = PlayServicesFallbackBarcodeScannerViewFactory()
    private val zxingFactory = ZxingBarcodeScannerViewFactory()

    override fun create(
        activity: Activity,
        lifecycleOwner: LifecycleOwner,
        qrOnly: Boolean,
        prompt: String,
        useFrontCamera: Boolean
    ): BarcodeScannerView {
        val factory = if (qrOnly || settings.getExperimentalOptIn(ProjectKeys.KEY_MLKIT_SCANNING)) {
            playServicesFallbackFactory
        } else {
            zxingFactory
        }

        return factory.create(activity, lifecycleOwner, qrOnly, prompt, useFrontCamera)
    }
}
