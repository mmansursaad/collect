package com.jed.optima.settings.migration;

import static com.jed.optima.settings.migration.MigrationUtils.replace;

import com.jed.optima.shared.settings.Settings;

public class KeyRenamer implements Migration {

    String oldKey;
    String newKey;

    KeyRenamer(String oldKey) {
        this.oldKey = oldKey;
    }

    public KeyRenamer toKey(String newKey) {
        this.newKey = newKey;
        return this;
    }

    public void apply(Settings prefs) {
        if (prefs.contains(oldKey) && !prefs.contains(newKey)) {
            Object value = prefs.getAll().get(oldKey);
            replace(prefs, oldKey, newKey, value);
        }
    }
}
