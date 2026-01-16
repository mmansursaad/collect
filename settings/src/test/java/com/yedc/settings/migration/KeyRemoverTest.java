package com.yedc.settings.migration;

import static com.yedc.settings.migration.MigrationUtils.removeKey;
import static com.yedc.settings.support.SettingsUtils.assertSettingsEmpty;
import static com.yedc.settings.support.SettingsUtils.initSettings;

import org.junit.Test;
import com.yedc.shared.settings.InMemSettings;
import com.yedc.shared.settings.Settings;

public class KeyRemoverTest {

    private final Settings prefs = new InMemSettings();

    @Test
    public void whenKeyDoesNotExist_doesNothing() {
        initSettings(prefs);

        removeKey("blah").apply(prefs);

        assertSettingsEmpty(prefs);
    }
}
