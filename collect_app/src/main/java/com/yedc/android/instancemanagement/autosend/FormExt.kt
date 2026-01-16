package com.yedc.android.instancemanagement.autosend

fun com.yedc.forms.Form.shouldFormBeSentAutomatically(isAutoSendEnabledInSettings: Boolean): Boolean {
    return if (isAutoSendEnabledInSettings) {
        getAutoSendMode() != FormAutoSendMode.OPT_OUT
    } else {
        getAutoSendMode() == FormAutoSendMode.FORCED
    }
}

fun com.yedc.forms.Form.getAutoSendMode(): FormAutoSendMode {
    return if (autoSend?.trim()?.lowercase() == "false") {
        FormAutoSendMode.OPT_OUT
    } else if (autoSend?.trim()?.lowercase() == "true") {
        FormAutoSendMode.FORCED
    } else {
        FormAutoSendMode.NEUTRAL
    }
}

fun com.yedc.forms.Form.getLastUpdated(): Long {
    return lastDetectedAttachmentsUpdateDate ?: date
}

enum class FormAutoSendMode {
    OPT_OUT,
    FORCED,
    NEUTRAL
}
