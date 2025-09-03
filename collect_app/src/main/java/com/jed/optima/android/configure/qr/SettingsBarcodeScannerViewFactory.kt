package com.jed.optima.android.configure.qr

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import com.jed.optima.android.preferences.SettingsExt.getExperimentalOptIn
import com.jed.optima.qrcode.BarcodeScannerView
import com.jed.optima.qrcode.BarcodeScannerViewContainer
import com.jed.optima.qrcode.mlkit.PlayServicesFallbackBarcodeScannerViewFactory
import com.jed.optima.qrcode.zxing.ZxingBarcodeScannerViewFactory
import com.jed.optima.settings.keys.ProjectKeys
import com.jed.optima.shared.settings.Settings

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
