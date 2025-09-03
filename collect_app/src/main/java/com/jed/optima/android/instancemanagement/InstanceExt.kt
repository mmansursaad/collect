package com.jed.optima.android.instancemanagement

import android.content.Context
import android.content.res.Resources
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.settings.keys.ProtectedProjectKeys
import com.jed.optima.strings.R
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun com.jed.optima.forms.instances.Instance.canBeEdited(settingsProvider: SettingsProvider): Boolean {
    return isDraft() &&
        settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_EDIT_SAVED)
}

fun com.jed.optima.forms.instances.Instance.getStatusDescription(resources: Resources): String {
    return getStatusDescription(resources, status, Date(lastStatusChangeDate))
}

fun getStatusDescription(context: Context, state: String?, date: Date): String {
    return getStatusDescription(context.resources, state, date)
}

fun com.jed.optima.forms.instances.Instance.isDraft(): Boolean {
    return draftStatuses.contains(status)
}

fun com.jed.optima.forms.instances.Instance.isEdit(): Boolean {
    return editOf != null
}

fun com.jed.optima.forms.instances.Instance.isDeletable(): Boolean {
    return canDeleteBeforeSend() || status != com.jed.optima.forms.instances.Instance.STATUS_COMPLETE && status != com.jed.optima.forms.instances.Instance.STATUS_SUBMISSION_FAILED
}

fun com.jed.optima.forms.instances.Instance.showAsEditable(settingsProvider: SettingsProvider): Boolean {
    return isDraft() && settingsProvider.getProtectedSettings()
        .getBoolean(ProtectedProjectKeys.KEY_EDIT_SAVED)
}

fun com.jed.optima.forms.instances.Instance.userVisibleInstanceName(
    resources: Resources = com.jed.optima.android.application.Collect.getInstance().resources
): String {
    return if (isEdit()) {
        resources.getString(R.string.user_visible_instance_name, displayName, editNumber)
    } else {
        displayName
    }
}

private fun getStatusDescription(resources: Resources, state: String?, date: Date): String {
    return try {
        if (com.jed.optima.forms.instances.Instance.STATUS_INCOMPLETE.equals(state, ignoreCase = true) ||
            com.jed.optima.forms.instances.Instance.STATUS_INVALID.equals(state, ignoreCase = true) ||
            com.jed.optima.forms.instances.Instance.STATUS_VALID.equals(state, ignoreCase = true) ||
            com.jed.optima.forms.instances.Instance.STATUS_NEW_EDIT.equals(state, ignoreCase = true)
        ) {
            SimpleDateFormat(
                resources.getString(R.string.saved_on_date_at_time),
                Locale.getDefault()
            ).format(date)
        } else if (com.jed.optima.forms.instances.Instance.STATUS_COMPLETE.equals(state, ignoreCase = true)) {
            SimpleDateFormat(
                resources.getString(R.string.finalized_on_date_at_time),
                Locale.getDefault()
            ).format(date)
        } else if (com.jed.optima.forms.instances.Instance.STATUS_SUBMITTED.equals(state, ignoreCase = true)) {
            SimpleDateFormat(
                resources.getString(R.string.sent_on_date_at_time),
                Locale.getDefault()
            ).format(date)
        } else if (com.jed.optima.forms.instances.Instance.STATUS_SUBMISSION_FAILED.equals(state, ignoreCase = true)) {
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

fun com.jed.optima.forms.instances.Instance.getIcon(): Int {
    return getInstanceIcon(this.status)
}

fun getInstanceIcon(status: String): Int {
    return when (status) {
        com.jed.optima.forms.instances.Instance.STATUS_INCOMPLETE, com.jed.optima.forms.instances.Instance.STATUS_INVALID, com.jed.optima.forms.instances.Instance.STATUS_VALID, com.jed.optima.forms.instances.Instance.STATUS_NEW_EDIT -> com.jed.optima.android.R.drawable.ic_form_state_saved
        com.jed.optima.forms.instances.Instance.STATUS_COMPLETE -> com.jed.optima.android.R.drawable.ic_form_state_finalized
        com.jed.optima.forms.instances.Instance.STATUS_SUBMITTED -> com.jed.optima.android.R.drawable.ic_form_state_submitted
        com.jed.optima.forms.instances.Instance.STATUS_SUBMISSION_FAILED -> com.jed.optima.android.R.drawable.ic_form_state_submission_failed
        else -> throw IllegalArgumentException()
    }
}

private val draftStatuses = arrayOf(
    com.jed.optima.forms.instances.Instance.STATUS_INCOMPLETE,
    com.jed.optima.forms.instances.Instance.STATUS_INVALID,
    com.jed.optima.forms.instances.Instance.STATUS_VALID,
    com.jed.optima.forms.instances.Instance.STATUS_NEW_EDIT
)
