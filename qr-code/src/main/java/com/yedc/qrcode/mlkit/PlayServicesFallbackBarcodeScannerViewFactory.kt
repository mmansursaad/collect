package com.yedc.qrcode.mlkit

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import com.yedc.qrcode.BarcodeScannerView
import com.yedc.qrcode.BarcodeScannerViewContainer
import com.yedc.qrcode.zxing.ZxingBarcodeScannerViewFactory

class PlayServicesFallbackBarcodeScannerViewFactory : BarcodeScannerViewContainer.Factory {

    private val mlKitBarcodeScannerViewFactory = MlKitBarcodeScannerViewFactory()
    private val zxingBarcodeScannerViewFactory = ZxingBarcodeScannerViewFactory()

    override fun create(
        activity: Activity,
        lifecycleOwner: LifecycleOwner,
        qrOnly: Boolean,
        prompt: String,
        useFrontCamera: Boolean
    ): BarcodeScannerView {
        if (MlKitBarcodeScannerViewFactory.isAvailable()) {
            return mlKitBarcodeScannerViewFactory.create(
                activity,
                lifecycleOwner,
                qrOnly,
                prompt,
                useFrontCamera
            )
        } else {
            return zxingBarcodeScannerViewFactory.create(
                activity,
                lifecycleOwner,
                qrOnly,
                prompt,
                useFrontCamera
            )
        }
    }
}
