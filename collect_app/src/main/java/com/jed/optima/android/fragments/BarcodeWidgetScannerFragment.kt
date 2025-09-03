package com.jed.optima.android.fragments

import com.jed.optima.externalapp.ExternalAppUtils.returnSingleValue

class BarcodeWidgetScannerFragment : com.jed.optima.android.fragments.BarCodeScannerFragment() {
    override fun isQrOnly(): Boolean {
        return false
    }

    override fun handleScanningResult(result: String) {
        returnSingleValue(requireActivity(), result)
    }
}
