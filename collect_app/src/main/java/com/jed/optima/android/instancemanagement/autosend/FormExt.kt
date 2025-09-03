package com.jed.optima.android.instancemanagement.autosend

fun com.jed.optima.forms.Form.shouldFormBeSentAutomatically(isAutoSendEnabledInSettings: Boolean): Boolean {
    return if (isAutoSendEnabledInSettings) {
        getAutoSendMode() != FormAutoSendMode.OPT_OUT
    } else {
        getAutoSendMode() == FormAutoSendMode.FORCED
    }
}

fun com.jed.optima.forms.Form.getAutoSendMode(): FormAutoSendMode {
    return if (autoSend?.trim()?.lowercase() == "false") {
        FormAutoSendMode.OPT_OUT
    } else if (autoSend?.trim()?.lowercase() == "true") {
        FormAutoSendMode.FORCED
    } else {
        FormAutoSendMode.NEUTRAL
    }
}

fun com.jed.optima.forms.Form.getLastUpdated(): Long {
    return lastDetectedAttachmentsUpdateDate ?: date
}

enum class FormAutoSendMode {
    OPT_OUT,
    FORCED,
    NEUTRAL
}
