package com.yedc.android.preferences.screens

import android.os.Bundle
import androidx.preference.Preference
import com.yedc.android.R
import com.yedc.settings.enums.StringIdEnumUtils.getFormUpdateMode
import com.yedc.settings.keys.ProtectedProjectKeys

class MainMenuAccessPreferencesFragment : _root_ide_package_.com.yedc.android.preferences.screens.BaseAdminPreferencesFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.main_menu_access_preferences, rootKey)

        findPreference<Preference>(ProtectedProjectKeys.KEY_EDIT_SAVED)!!.isEnabled =
            settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.ALLOW_OTHER_WAYS_OF_EDITING_FORM)

        val formUpdateMode = settingsProvider.getUnprotectedSettings().getFormUpdateMode(requireContext())
        if (formUpdateMode == com.yedc.settings.enums.FormUpdateMode.MATCH_EXACTLY) {
            _root_ide_package_.com.yedc.android.preferences.utilities.PreferencesUtils.displayDisabled(findPreference(ProtectedProjectKeys.KEY_GET_BLANK), false)
        }
    }
}
