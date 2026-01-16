package com.yedc.android.instancemanagement

import android.content.Context
import android.content.res.Resources
import com.yedc.settings.SettingsProvider
import com.yedc.settings.keys.ProtectedProjectKeys
import com.yedc.strings.R
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun com.yedc.forms.instances.Instance.canBeEdited(settingsProvider: SettingsProvider): Boolean {
    return isDraft() &&
        settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_EDIT_SAVED)
}

fun com.yedc.forms.instances.Instance.getStatusDescription(resources: Resources): String {
    return getStatusDescription(resources, status, Date(lastStatusChangeDate))
}

fun getStatusDescription(context: Context, state: String?, date: Date): String {
    return getStatusDescription(context.resources, state, date)
}

fun com.yedc.forms.instances.Instance.isDraft(): Boolean {
    return draftStatuses.contains(status)
}

fun com.yedc.forms.instances.Instance.isEdit(): Boolean {
    return editOf != null
}

fun com.yedc.forms.instances.Instance.isDeletable(): Boolean {
    return canDeleteBeforeSend() || status != com.yedc.forms.instances.Instance.STATUS_COMPLETE && status != com.yedc.forms.instances.Instance.STATUS_SUBMISSION_FAILED
}

fun com.yedc.forms.instances.Instance.showAsEditable(settingsProvider: SettingsProvider): Boolean {
    return isDraft() && settingsProvider.getProtectedSettings()
        .getBoolean(ProtectedProjectKeys.KEY_EDIT_SAVED)
}

fun com.yedc.forms.instances.Instance.userVisibleInstanceName(
    resources: Resources = _root_ide_package_.com.yedc.android.application.Collect.getInstance().resources
): String {
    return if (isEdit()) {
        resources.getString(R.string.user_visible_instance_name, displayName, editNumber)
    } else {
        displayName
    }
}

private fun getStatusDescription(resources: Resources, state: String?, date: Date): String {
    return try {
        if (com.yedc.forms.instances.Instance.STATUS_INCOMPLETE.equals(state, ignoreCase = true) ||
            com.yedc.forms.instances.Instance.STATUS_INVALID.equals(state, ignoreCase = true) ||
            com.yedc.forms.instances.Instance.STATUS_VALID.equals(state, ignoreCase = true) ||
            com.yedc.forms.instances.Instance.STATUS_NEW_EDIT.equals(state, ignoreCase = true)
        ) {
            SimpleDateFormat(
                resources.getString(R.string.saved_on_date_at_time),
                Locale.getDefault()
            ).format(date)
        } else if (com.yedc.forms.instances.Instance.STATUS_COMPLETE.equals(state, ignoreCase = true)) {
            SimpleDateFormat(
                resources.getString(R.string.finalized_on_date_at_time),
                Locale.getDefault()
            ).format(date)
        } else if (com.yedc.forms.instances.Instance.STATUS_SUBMITTED.equals(state, ignoreCase = true)) {
            SimpleDateFormat(
                resources.getString(R.string.sent_on_date_at_time),
                Locale.getDefault()
            ).format(date)
        } else if (com.yedc.forms.instances.Instance.STATUS_SUBMISSION_FAILED.equals(state, ignoreCase = true)) {
            SimpleDateFormat(
                resources.getString(R.string.sending_failed_on_date_at_time),
                Locale.getDefault()
            ).format(date)
        } else {
            SimpleDateFormat(
                resources.getString(R.string.added_on_date_at_time),
                Locale.getDefault()
            ).format(date)
        }
    } catch (e: IllegalArgumentException) {
        Timber.e(e, "Current locale: %s", Locale.getDefault())
        ""
    }
}

fun com.yedc.forms.instances.Instance.getIcon(): Int {
    return getInstanceIcon(this.status)
}

fun getInstanceIcon(status: String): Int {
    return when (status) {
        com.yedc.forms.instances.Instance.STATUS_INCOMPLETE, com.yedc.forms.instances.Instance.STATUS_INVALID, com.yedc.forms.instances.Instance.STATUS_VALID, com.yedc.forms.instances.Instance.STATUS_NEW_EDIT -> com.yedc.android.R.drawable.ic_form_state_saved
        com.yedc.forms.instances.Instance.STATUS_COMPLETE -> com.yedc.android.R.drawable.ic_form_state_finalized
        com.yedc.forms.instances.Instance.STATUS_SUBMITTED -> com.yedc.android.R.drawable.ic_form_state_submitted
        com.yedc.forms.instances.Instance.STATUS_SUBMISSION_FAILED -> com.yedc.android.R.drawable.ic_form_state_submission_failed
        else -> throw IllegalArgumentException()
    }
}

private val draftStatuses = arrayOf(
    com.yedc.forms.instances.Instance.STATUS_INCOMPLETE,
    com.yedc.forms.instances.Instance.STATUS_INVALID,
    com.yedc.forms.instances.Instance.STATUS_VALID,
    com.yedc.forms.instances.Instance.STATUS_NEW_EDIT
)
