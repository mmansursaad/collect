package com.jed.optima.android.preferences.screens

import android.os.Bundle
import androidx.preference.Preference
import com.jed.optima.android.R
import com.jed.optima.settings.enums.StringIdEnumUtils.getFormUpdateMode
import com.jed.optima.settings.keys.ProtectedProjectKeys

class MainMenuAccessPreferencesFragment : com.jed.optima.android.preferences.screens.BaseAdminPreferencesFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.main_menu_access_preferences, rootKey)

        findPreference<Preference>(ProtectedProjectKeys.KEY_EDIT_SAVED)!!.isEnabled =
            settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.ALLOW_OTHER_WAYS_OF_EDITING_FORM)

        val formUpdateMode = settingsProvider.getUnprotectedSettings().getFormUpdateMode(requireContext())
        if (formUpdateMode == com.jed.optima.settings.enums.FormUpdateMode.MATCH_EXACTLY) {
            com.jed.optima.android.preferences.utilities.PreferencesUtils.displayDisabled(findPreference(ProtectedProjectKeys.KEY_GET_BLANK), false)
        }
    }
}
