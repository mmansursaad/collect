package com.yedc.android.preferences.screens

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yedc.analytics.Analytics
import com.yedc.android.R
import com.yedc.android.activities.FirstLaunchActivity
import com.yedc.android.analytics.AnalyticsEvents
import com.yedc.android.configure.qr.QRCodeTabsActivity
import com.yedc.android.injection.DaggerUtils
import com.yedc.android.mainmenu.MainMenuActivity
import com.yedc.android.projects.DeleteProjectResult
import com.yedc.android.projects.ProjectDeleter
import com.yedc.androidshared.ui.ToastUtils
import com.yedc.androidshared.ui.multiclicksafe.MultiClickGuard
import javax.inject.Inject

class ProjectManagementPreferencesFragment :
    _root_ide_package_.com.yedc.android.preferences.screens.BaseAdminPreferencesFragment(),
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
            var resetDialogPreference: _root_ide_package_.com.yedc.android.preferences.dialogs.ResetDialogPreference? = null
            if (preference is _root_ide_package_.com.yedc.android.preferences.dialogs.ResetDialogPreference) {
                resetDialogPreference = preference
            }
            if (resetDialogPreference != null) {
                val dialogFragment = _root_ide_package_.com.yedc.android.preferences.dialogs.ResetDialogPreferenceFragmentCompat.newInstance(preference.key)
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
                        .setTitle(com.yedc.strings.R.string.delete_project)
                        .setMessage(com.yedc.strings.R.string.delete_project_confirm_message)
                        .setNegativeButton(com.yedc.strings.R.string.delete_project_no) { _: DialogInterface?, _: Int -> }
                        .setPositiveButton(com.yedc.strings.R.string.delete_project_yes) { _: DialogInterface?, _: Int -> deleteProject() }
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
                    .setTitle(com.yedc.strings.R.string.cannot_delete_project_title)
                    .setMessage(com.yedc.strings.R.string.cannot_delete_project_message_one)
                    .setPositiveButton(com.yedc.strings.R.string.ok, null)
                    .show()
            }
            is DeleteProjectResult.RunningBackgroundJobs -> {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(com.yedc.strings.R.string.cannot_delete_project_title)
                    .setMessage(com.yedc.strings.R.string.cannot_delete_project_message_two)
                    .setPositiveButton(com.yedc.strings.R.string.ok, null)
                    .show()
            }
            is DeleteProjectResult.DeletedSuccessfullyCurrentProject -> {
                val newCurrentProject = deleteProjectResult.newCurrentProject
                _root_ide_package_.com.yedc.android.activities.ActivityUtils.startActivityAndCloseAllOthers(
                    requireActivity(),
                    MainMenuActivity::class.java
                )
                ToastUtils.showLongToast(
                    getString(
                        com.yedc.strings.R.string.switched_project,
                        newCurrentProject.name
                    )
                )
            }
            is DeleteProjectResult.DeletedSuccessfullyLastProject -> {
                _root_ide_package_.com.yedc.android.activities.ActivityUtils.startActivityAndCloseAllOthers(
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
