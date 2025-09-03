package com.jed.optima.settings.migration;

import com.jed.optima.shared.settings.Settings;

public interface Migration {
    void apply(Settings prefs);
}
