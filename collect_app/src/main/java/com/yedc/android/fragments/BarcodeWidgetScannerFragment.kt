package com.yedc.android.fragments

import com.yedc.externalapp.ExternalAppUtils.returnSingleValue

class BarcodeWidgetScannerFragment : _root_ide_package_.com.yedc.android.fragments.BarCodeScannerFragment() {
    override fun isQrOnly(): Boolean {
        return false
    }

    override fun handleScanningResult(result: String) {
        returnSingleValue(requireActivity(), result)
    }
}
