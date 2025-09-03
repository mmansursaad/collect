package com.jed.optima.android.utilities;

import static com.jed.optima.settings.keys.ProtectedProjectKeys.KEY_ADMIN_PW;

import com.jed.optima.shared.settings.Settings;

public class AdminPasswordProvider {
    private final Settings adminSettings;

    public AdminPasswordProvider(Settings adminSettings) {
        this.adminSettings = adminSettings;
    }

    public boolean isAdminPasswordSet() {
        String adminPassword = getAdminPassword();
        return adminPassword != null && !adminPassword.isEmpty();
    }

    public String getAdminPassword() {
        return adminSettings.getString(KEY_ADMIN_PW);
    }
}
