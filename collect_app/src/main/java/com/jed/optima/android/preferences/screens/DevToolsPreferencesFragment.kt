package com.jed.optima.android.preferences.screens

import android.os.Bundle
import androidx.preference.Preference
import com.jed.optima.android.R

class DevToolsPreferencesFragment : com.jed.optima.android.preferences.screens.BaseProjectPreferencesFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.dev_tools_preferences, rootKey)

        findPreference<Preference>("crash_app")?.setOnPreferenceClickListener {
            throw RuntimeException("Simulated crash")
        }
    }
}
