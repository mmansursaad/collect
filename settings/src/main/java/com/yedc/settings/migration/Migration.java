package com.yedc.settings.migration;

import com.yedc.shared.settings.Settings;

public interface Migration {
    void apply(Settings prefs);
}
