package com.jed.optima.settings.migration;

import static com.jed.optima.settings.migration.MigrationUtils.removeKey;
import static com.jed.optima.settings.support.SettingsUtils.assertSettingsEmpty;
import static com.jed.optima.settings.support.SettingsUtils.initSettings;

import org.junit.Test;
import com.jed.optima.shared.settings.InMemSettings;
import com.jed.optima.shared.settings.Settings;

public class KeyRemoverTest {

    private final Settings prefs = new InMemSettings();

    @Test
    public void whenKeyDoesNotExist_doesNothing() {
        initSettings(prefs);

        removeKey("blah").apply(prefs);

        assertSettingsEmpty(prefs);
    }
}
