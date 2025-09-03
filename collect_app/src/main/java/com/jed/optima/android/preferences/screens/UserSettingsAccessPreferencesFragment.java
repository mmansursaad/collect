package com.jed.optima.android.preferences.screens;

import android.os.Bundle;

import androidx.preference.PreferenceCategory;

import com.jed.optima.android.R;
import com.jed.optima.android.application.FeatureFlags;
import com.jed.optima.settings.keys.ProtectedProjectKeys;

public class UserSettingsAccessPreferencesFragment extends BaseAdminPreferencesFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.user_settings_access_preferences, rootKey);

        if (FeatureFlags.NO_THEME_SETTING) {
            PreferenceCategory category = (PreferenceCategory) getPreferenceScreen().getPreference(0);
            category.removePreference(findPreference(ProtectedProjectKeys.KEY_APP_THEME));
        }
    }
}
