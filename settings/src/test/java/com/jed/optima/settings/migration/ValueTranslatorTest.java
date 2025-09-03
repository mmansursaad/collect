package com.jed.optima.settings.migration;

import static com.jed.optima.settings.migration.MigrationUtils.translateValue;
import static com.jed.optima.settings.support.SettingsUtils.assertSettings;
import static com.jed.optima.settings.support.SettingsUtils.initSettings;

import org.junit.Test;
import com.jed.optima.shared.settings.InMemSettings;
import com.jed.optima.shared.settings.Settings;

public class ValueTranslatorTest {

    private final Settings prefs = new InMemSettings();

    @Test
    public void translatesValueForKey() {
        initSettings(prefs,
                "key", "value"
        );

        translateValue("value").toValue("newValue").forKey("key").apply(prefs);

        assertSettings(prefs,
                "key", "newValue"
        );
    }

    @Test
    public void doesNotTranslateOtherValues() {
        initSettings(prefs,
                "key", "otherValue"
        );

        translateValue("value").toValue("newValue").forKey("key").apply(prefs);

        assertSettings(prefs,
                "key", "otherValue"
        );
    }

    @Test
    public void whenKeyNotInPrefs_doesNothing() {
        initSettings(prefs,
                "otherKey", "value"
        );

        translateValue("value").toValue("newValue").forKey("key").apply(prefs);

        assertSettings(prefs,
                "otherKey", "value"
        );
    }
}
