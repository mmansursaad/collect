package com.jed.optima.mapbox;

import static com.jed.optima.settings.keys.ProjectKeys.KEY_MAPBOX_MAP_STYLE;
import static kotlin.collections.SetsKt.setOf;

import android.content.Context;
import android.os.Bundle;

import androidx.preference.Preference;

import com.mapbox.maps.Style;

import com.jed.optima.androidshared.system.OpenGLVersionChecker;
import com.jed.optima.androidshared.ui.PrefUtils;
import com.jed.optima.androidshared.ui.ToastUtils;
import com.jed.optima.maps.MapConfigurator;
import com.jed.optima.maps.layers.MbtilesFile;
import com.jed.optima.settings.keys.ProjectKeys;
import com.jed.optima.shared.settings.Settings;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MapboxMapConfigurator implements MapConfigurator {
    private final String prefKey;
    private final int sourceLabelId;
    private final MapboxUrlOption[] options;

    /** Constructs a configurator with a few Mapbox style URL options to choose from. */
    public MapboxMapConfigurator() {
        this.prefKey = KEY_MAPBOX_MAP_STYLE;
        this.sourceLabelId = com.jed.optima.strings.R.string.basemap_source_mapbox;
        this.options = new MapboxUrlOption[]{
                new MapboxUrlOption(Style.MAPBOX_STREETS, com.jed.optima.strings.R.string.streets),
                new MapboxUrlOption(Style.LIGHT, com.jed.optima.strings.R.string.light),
                new MapboxUrlOption(Style.DARK, com.jed.optima.strings.R.string.dark),
                new MapboxUrlOption(Style.SATELLITE, com.jed.optima.strings.R.string.satellite),
                new MapboxUrlOption(Style.SATELLITE_STREETS, com.jed.optima.strings.R.string.hybrid),
                new MapboxUrlOption(Style.OUTDOORS, com.jed.optima.strings.R.string.outdoors)
        };
    }

    @Override public boolean isAvailable(Context context) {
        /*
         * The Mapbox SDK for Android requires OpenGL ES version 3.
         * See: https://github.com/mapbox/mapbox-maps-android/blob/main/CHANGELOG.md#1100-november-29-2023
         */
        return OpenGLVersionChecker.isOpenGLv3Supported(context);
    }

    @Override public void showUnavailableMessage(Context context) {
        ToastUtils.showLongToast(context.getString(com.jed.optima.strings.R.string.basemap_source_unavailable, context.getString(sourceLabelId)));
    }

    @Override public List<Preference> createPrefs(Context context, Settings settings) {
        int[] labelIds = new int[options.length];
        String[] values = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            labelIds[i] = options[i].labelId;
            values[i] = options[i].url;
        }
        String prefTitle = context.getString(
            com.jed.optima.strings.R.string.map_style_label, context.getString(sourceLabelId));
        return Collections.singletonList(PrefUtils.createListPref(
            context, prefKey, prefTitle, labelIds, values, settings
        ));
    }

    @Override public Set<String> getPrefKeys() {
        return prefKey.isEmpty() ? setOf(ProjectKeys.KEY_REFERENCE_LAYER) :
                setOf(prefKey, ProjectKeys.KEY_REFERENCE_LAYER);
    }

    @Override public Bundle buildConfig(Settings prefs) {
        Bundle config = new Bundle();
        config.putString(MapboxMapFragment.KEY_STYLE_URL,
            prefs.getString(ProjectKeys.KEY_MAPBOX_MAP_STYLE));
        config.putString(MapboxMapFragment.KEY_REFERENCE_LAYER,
            prefs.getString(ProjectKeys.KEY_REFERENCE_LAYER));
        return config;
    }

    @Override public boolean supportsLayer(File file) {
        // MapboxMapFragment supports any file that MbtilesFile can read.
        return MbtilesFile.readLayerType(file) != null;
    }

    @Override public String getDisplayName(File file) {
        String name = MbtilesFile.readName(file);
        return name != null ? name : file.getName();
    }

    static class MapboxUrlOption {
        final String url;
        final int labelId;

        MapboxUrlOption(String url, int labelId) {
            this.url = url;
            this.labelId = labelId;
        }
    }
}
