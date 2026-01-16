package com.yedc.android.preferences.dialogs

import android.content.Context
import com.yedc.android.R
import com.yedc.strings.localization.getLocalizedString

class ResetProgressDialog : com.yedc.material.MaterialProgressDialogFragment() {
    override fun onAttach(context: Context) {
        super.onAttach(context)

        setTitle(context.getLocalizedString(com.yedc.strings.R.string.please_wait))
        setMessage(context.getLocalizedString(com.yedc.strings.R.string.reset_in_progress))
        isCancelable = false
    }
}
