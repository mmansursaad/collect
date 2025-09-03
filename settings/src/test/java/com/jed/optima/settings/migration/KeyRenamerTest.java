package com.jed.optima.settings.migration;

import static com.jed.optima.settings.migration.MigrationUtils.renameKey;
import static com.jed.optima.settings.support.SettingsUtils.assertSettings;
import static com.jed.optima.settings.support.SettingsUtils.initSettings;

import org.junit.Test;
import com.jed.optima.shared.settings.InMemSettings;
import com.jed.optima.shared.settings.Settings;

public class KeyRenamerTest {

    private final Settings prefs = new InMemSettings();

    @Test
    public void renamesKeys() {
        initSettings(prefs,
                "colour", "red"
        );

        renameKey("colour")
                .toKey("couleur")
                .apply(prefs);

        assertSettings(prefs,
                "couleur", "red"
        );
    }

    @Test
    public void whenNewKeyExists_doesNotDoAnything() {
        initSettings(prefs,
                "colour", "red",
                "couleur", "blue"
        );

        renameKey("colour")
                .toKey("couleur")
                .apply(prefs);

        assertSettings(prefs,
                "colour", "red",
                "couleur", "blue"
        );
    }
}
