package com.yedc.settings.migration;

import static com.yedc.settings.migration.MigrationUtils.translateKey;
import static com.yedc.settings.support.SettingsUtils.assertSettings;
import static com.yedc.settings.support.SettingsUtils.initSettings;

import org.junit.Test;
import com.yedc.shared.settings.InMemSettings;
import com.yedc.shared.settings.Settings;

public class KeyTranslatorTest {

    private final Settings prefs = new InMemSettings();

    @Test
    public void renamesKeyAndTranslatesValues() {
        initSettings(prefs,
                "colour", "red"
        );

        translateKey("colour")
                .toKey("couleur")
                .fromValue("red")
                .toValue("rouge")
                .apply(prefs);

        assertSettings(prefs,
                "couleur", "rouge"
        );
    }

    @Test
    public void canTranslateMultipleValues() {
        KeyTranslator translator = translateKey("colour")
                .toKey("couleur")
                .fromValue("red")
                .toValue("rouge")
                .fromValue("green")
                .toValue("vert");

        initSettings(prefs,
                "colour", "red"
        );

        translator.apply(prefs);

        assertSettings(prefs,
                "couleur", "rouge"
        );

        initSettings(prefs,
                "colour", "green"
        );

        translator.apply(prefs);

        assertSettings(prefs,
                "couleur", "vert"
        );
    }

    @Test
    public void whenKeyHasUnknownValue_doesNotDoAnything() {
        initSettings(prefs,
                "colour", "blue"
        );

        translateKey("color")
                .toKey("coleur")
                .fromValue("red")
                .toValue("rouge")
                .apply(prefs);

        assertSettings(prefs,
                "colour", "blue"
        );
    }

    @Test
    public void whenNewKeyExists_doesNotDoAnything() {
        initSettings(prefs,
                "colour", "red",
                "couleur", "bleu"
        );

        translateKey("color")
                .toKey("coleur")
                .fromValue("red")
                .toValue("rouge")
                .apply(prefs);

        assertSettings(prefs,
                "colour", "red",
                "couleur", "bleu"
        );
    }
}
