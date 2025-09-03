package com.jed.optima.android.preferences.dialogs

import android.content.Context
import com.jed.optima.android.R
import com.jed.optima.strings.localization.getLocalizedString

class ResetProgressDialog : com.jed.optima.material.MaterialProgressDialogFragment() {
    override fun onAttach(context: Context) {
        super.onAttach(context)

        setTitle(context.getLocalizedString(com.jed.optima.strings.R.string.please_wait))
        setMessage(context.getLocalizedString(com.jed.optima.strings.R.string.reset_in_progress))
        isCancelable = false
    }
}
