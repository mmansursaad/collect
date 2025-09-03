package com.jed.optima.android.preferences.screens

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jed.optima.analytics.Analytics
import com.jed.optima.android.R
import com.jed.optima.android.activities.FirstLaunchActivity
import com.jed.optima.android.analytics.AnalyticsEvents
import com.jed.optima.android.configure.qr.QRCodeTabsActivity
import com.jed.optima.android.injection.DaggerUtils
import com.jed.optima.android.mainmenu.MainMenuActivity
import com.jed.optima.android.projects.DeleteProjectResult
import com.jed.optima.android.projects.ProjectDeleter
import com.jed.optima.androidshared.ui.ToastUtils
import com.jed.optima.androidshared.ui.multiclicksafe.MultiClickGuard
import javax.inject.Inject

class ProjectManagementPreferencesFragment :
    com.jed.optima.android.preferences.screens.BaseAdminPreferencesFragment(),
    Preference.OnPreferenceClickListener {

    @Inject
    lateinit var projectDeleter: ProjectDeleter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.project_management_preferences, rootKey)

        findPreference<Preference>(IMPORT_SETTINGS_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(DELETE_PROJECT_KEY)!!.onPreferenceClickListener = this
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (MultiClickGuard.allowClick(javaClass.name)) {
            var resetDialogPreference: com.jed.optima.android.preferences.dialogs.ResetDialogPreference? = null
            if (preference is com.jed.optima.android.preferences.dialogs.ResetDialogPreference) {
                resetDialogPreference = preference
            }
            if (resetDialogPreference != null) {
                val dialogFragment = com.jed.optima.android.preferences.dialogs.ResetDialogPreferenceFragmentCompat.newInstance(preference.key)
                dialogFragment.setTargetFragment(this, 0)
                dialogFragment.show(parentFragmentManager, null)
            } else {
                super.onDisplayPreferenceDialog(preference)
            }
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (MultiClickGuard.allowClick(javaClass.name)) {
            when (preference.key) {
                IMPORT_SETTINGS_KEY -> {
                    val pref = Intent(activity, QRCodeTabsActivity::class.java)
                    startActivity(pref)
                }
                DELETE_PROJECT_KEY -> {
                    MaterialAlertDialogBuilder(requireActivity())
                        .setTitle(com.jed.optima.strings.R.string.delete_project)
                        .setMessage(com.jed.optima.strings.R.string.delete_project_confirm_message)
                        .setNegativeButton(com.jed.optima.strings.R.string.delete_project_no) { _: DialogInterface?, _: Int -> }
                        .setPositiveButton(com.jed.optima.strings.R.string.delete_project_yes) { _: DialogInterface?, _: Int -> deleteProject() }
                        .show()
                }
            }
            return true
        }
        return false
    }

    private fun deleteProject() {
        Analytics.log(AnalyticsEvents.DELETE_PROJECT)

        when (val deleteProjectResult = projectDeleter.deleteProject()) {
            is DeleteProjectResult.UnsentInstances -> {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(com.jed.optima.strings.R.string.cannot_delete_project_title)
                    .setMessage(com.jed.optima.strings.R.string.cannot_delete_project_message_one)
                    .setPositiveButton(com.jed.optima.strings.R.string.ok, null)
                    .show()
            }
            is DeleteProjectResult.RunningBackgroundJobs -> {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(com.jed.optima.strings.R.string.cannot_delete_project_title)
                    .setMessage(com.jed.optima.strings.R.string.cannot_delete_project_message_two)
                    .setPositiveButton(com.jed.optima.strings.R.string.ok, null)
                    .show()
            }
            is DeleteProjectResult.DeletedSuccessfullyCurrentProject -> {
                val newCurrentProject = deleteProjectResult.newCurrentProject
                com.jed.optima.android.activities.ActivityUtils.startActivityAndCloseAllOthers(
                    requireActivity(),
                    MainMenuActivity::class.java
                )
                ToastUtils.showLongToast(
                    getString(
                        com.jed.optima.strings.R.string.switched_project,
                        newCurrentProject.name
                    )
                )
            }
            is DeleteProjectResult.DeletedSuccessfullyLastProject -> {
                com.jed.optima.android.activities.ActivityUtils.startActivityAndCloseAllOthers(
                    requireActivity(),
                    FirstLaunchActivity::class.java
                )
            }
            is DeleteProjectResult.DeletedSuccessfullyInactiveProject -> {
                // not possible here
            }
        }
    }

    companion object {
        const val IMPORT_SETTINGS_KEY = "import_settings"
        const val DELETE_PROJECT_KEY = "delete_project"
    }
}
