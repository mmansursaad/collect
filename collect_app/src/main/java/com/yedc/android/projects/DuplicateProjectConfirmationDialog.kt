package com.yedc.android.projects

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yedc.analytics.Analytics
import com.yedc.android.R
import com.yedc.android.analytics.AnalyticsEvents
import com.yedc.android.projects.DuplicateProjectConfirmationKeys.MATCHING_PROJECT
import com.yedc.android.projects.DuplicateProjectConfirmationKeys.SETTINGS_JSON

class DuplicateProjectConfirmationDialog : DialogFragment() {
    lateinit var listener: DuplicateProjectConfirmationListener

    interface DuplicateProjectConfirmationListener {
        fun createProject(settingsJson: String)
        fun switchToProject(uuid: String)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Analytics.log(AnalyticsEvents.DUPLICATE_PROJECT)
        listener = parentFragment as DuplicateProjectConfirmationListener

        val settingsJson = arguments?.getString(SETTINGS_JSON, "") ?: ""
        val matchingProject = arguments?.getString(MATCHING_PROJECT, "") ?: ""

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(com.yedc.strings.R.string.duplicate_project)
            .setMessage(com.yedc.strings.R.string.duplicate_project_details)
            .setPositiveButton(com.yedc.strings.R.string.add_duplicate_project) { _, _ -> listener.createProject(settingsJson) }
            .setNegativeButton(com.yedc.strings.R.string.switch_to_existing) { _, _ ->
                run {
                    listener.switchToProject(matchingProject)
                    Analytics.log(AnalyticsEvents.DUPLICATE_PROJECT_SWITCH)
                }
            }
            .create()
    }
}

object DuplicateProjectConfirmationKeys {
    const val SETTINGS_JSON = "settingsJson"
    const val MATCHING_PROJECT = "matchingProject"
}
