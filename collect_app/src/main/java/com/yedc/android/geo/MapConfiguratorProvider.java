package com.yedc.android.geo;

import static com.yedc.settings.keys.ProjectKeys.BASEMAP_SOURCE_CARTO;
import static com.yedc.settings.keys.ProjectKeys.BASEMAP_SOURCE_GOOGLE;
import static com.yedc.settings.keys.ProjectKeys.BASEMAP_SOURCE_MAPBOX;
import static com.yedc.settings.keys.ProjectKeys.BASEMAP_SOURCE_OSM;
import static com.yedc.settings.keys.ProjectKeys.BASEMAP_SOURCE_USGS;
import static com.yedc.settings.keys.ProjectKeys.KEY_BASEMAP_SOURCE;
import static com.yedc.settings.keys.ProjectKeys.KEY_CARTO_MAP_STYLE;
import static com.yedc.settings.keys.ProjectKeys.KEY_GOOGLE_MAP_STYLE;
import static com.yedc.settings.keys.ProjectKeys.KEY_USGS_MAP_STYLE;
import static com.yedc.strings.localization.LocalizedApplicationKt.getLocalizedString;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;

import com.yedc.android.application.Collect;
import com.yedc.android.application.MapboxClassInstanceCreator;
import com.yedc.googlemaps.GoogleMapConfigurator;
import com.yedc.googlemaps.GoogleMapConfigurator.GoogleMapTypeOption;
import com.yedc.android.injection.DaggerUtils;
import com.yedc.maps.MapConfigurator;
import com.yedc.osmdroid.OsmDroidMapConfigurator;
import com.yedc.osmdroid.OsmDroidMapConfigurator.WmsOption;
import com.yedc.osmdroid.WebMapService;

import java.util.ArrayList;

public class MapConfiguratorProvider {

    private static SourceOption[] sourceOptions;
    private static final String USGS_URL_BASE =
        "https://basemap.nationalmap.gov/arcgis/rest/services";
    private static final String OSM_COPYRIGHT = "© OpenStreetMap contributors";
    private static final String CARTO_COPYRIGHT = "© CARTO";
    private static final String CARTO_ATTRIBUTION = OSM_COPYRIGHT + ", " + CARTO_COPYRIGHT;
    private static final String USGS_ATTRIBUTION = "Map services and data available from U.S. Geological Survey,\nNational Geospatial Program.";

    private MapConfiguratorProvider() {

    }

    /**
     * In the preference UI, the available basemaps are organized into "sources"
     * to make them easier to find.  This defines the basemap sources and the
     * basemap options available under each one, in their order of appearance.
     */
    public static void initOptions(Context context) {
        if (sourceOptions != null) {
            return;
        }

        ArrayList<SourceOption> sourceOptions = new ArrayList<>();

        GoogleMapConfigurator googleMapsConfigurator = new GoogleMapConfigurator(
                KEY_GOOGLE_MAP_STYLE, com.yedc.strings.R.string.basemap_source_google,
                new GoogleMapTypeOption(GoogleMap.MAP_TYPE_NORMAL, com.yedc.strings.R.string.streets),
                new GoogleMapTypeOption(GoogleMap.MAP_TYPE_TERRAIN, com.yedc.strings.R.string.terrain),
                new GoogleMapTypeOption(GoogleMap.MAP_TYPE_HYBRID, com.yedc.strings.R.string.hybrid),
                new GoogleMapTypeOption(GoogleMap.MAP_TYPE_SATELLITE, com.yedc.strings.R.string.satellite)
        );

        if (googleMapsConfigurator.isAvailable(context)) {
            sourceOptions.add(new SourceOption(BASEMAP_SOURCE_GOOGLE, com.yedc.strings.R.string.basemap_source_google,
                    googleMapsConfigurator
            ));
        }

        if (isMapboxSupported()) {
            sourceOptions.add(new SourceOption(BASEMAP_SOURCE_MAPBOX, com.yedc.strings.R.string.basemap_source_mapbox,
                    MapboxClassInstanceCreator.createMapboxMapConfigurator()
            ));
        }

        sourceOptions.add(new SourceOption(BASEMAP_SOURCE_OSM, com.yedc.strings.R.string.basemap_source_osm,
                new OsmDroidMapConfigurator(
                        new WebMapService(
                                "Mapnik", 0, 19, 256, OSM_COPYRIGHT,
                                "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
                        )
                )
        ));
        sourceOptions.add(new SourceOption(BASEMAP_SOURCE_USGS, com.yedc.strings.R.string.basemap_source_usgs,
                new OsmDroidMapConfigurator(
                        KEY_USGS_MAP_STYLE, com.yedc.strings.R.string.basemap_source_usgs,
                        new WmsOption("topographic", com.yedc.strings.R.string.topographic, new WebMapService(
                                getLocalizedString(getApplication(), com.yedc.strings.R.string.openmap_usgs_topo), 0, 18, 256, USGS_ATTRIBUTION,
                                USGS_URL_BASE + "/USGSTopo/MapServer/tile/{z}/{y}/{x}"
                        )),
                        new WmsOption("hybrid", com.yedc.strings.R.string.hybrid, new WebMapService(
                                getLocalizedString(getApplication(), com.yedc.strings.R.string.openmap_usgs_sat), 0, 18, 256, USGS_ATTRIBUTION,
                                USGS_URL_BASE + "/USGSImageryTopo/MapServer/tile/{z}/{y}/{x}"
                        )),
                        new WmsOption("satellite", com.yedc.strings.R.string.satellite, new WebMapService(
                                getLocalizedString(getApplication(), com.yedc.strings.R.string.openmap_usgs_img), 0, 18, 256, USGS_ATTRIBUTION,
                                USGS_URL_BASE + "/USGSImageryOnly/MapServer/tile/{z}/{y}/{x}"
                        ))
                )
        ));
        sourceOptions.add(new SourceOption(BASEMAP_SOURCE_CARTO, com.yedc.strings.R.string.basemap_source_carto,
                new OsmDroidMapConfigurator(
                        KEY_CARTO_MAP_STYLE, com.yedc.strings.R.string.basemap_source_carto,
                        new WmsOption("positron", com.yedc.strings.R.string.carto_map_style_positron, new WebMapService(
                                getLocalizedString(getApplication(), com.yedc.strings.R.string.openmap_cartodb_positron), 0, 18, 256, CARTO_ATTRIBUTION,
                                "http://1.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"
                        )),
                        new WmsOption("dark_matter", com.yedc.strings.R.string.carto_map_style_dark_matter, new WebMapService(
                                getLocalizedString(getApplication(), com.yedc.strings.R.string.openmap_cartodb_darkmatter), 0, 18, 256, CARTO_ATTRIBUTION,
                                "http://1.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png"
                        ))
                )
        ));

        MapConfiguratorProvider.sourceOptions = sourceOptions.toArray(new SourceOption[]{});
    }

    /** Gets the currently selected MapConfigurator. */
    public static @NonNull
    MapConfigurator getConfigurator() {
        return getOption(null).cftor;
    }

    /**
     * Gets the MapConfigurator for the SourceOption with the given id, or the
     * currently selected MapConfigurator if id is null.
     */
    public static @NonNull MapConfigurator getConfigurator(String id) {
        return getOption(id).cftor;
    }

    /** Gets a list of the IDs of the basemap sources, in order. */
    public static String[] getIds() {
        String[] ids = new String[sourceOptions.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = sourceOptions[i].id;
        }
        return ids;
    }

    /** Gets a list of the label string IDs of the basemap sources, in order. */
    public static int[] getLabelIds() {
        int[] labelIds = new int[sourceOptions.length];
        for (int i = 0; i < labelIds.length; i++) {
            labelIds[i] = sourceOptions[i].labelId;
        }
        return labelIds;
    }

    private static boolean isMapboxSupported() {
        return MapboxClassInstanceCreator.isMapboxAvailable();
    }

    /**
     * Gets the SourceOption with the given id, or the currently selected option
     * if id is null, or the first option if the id is unknown.  Never null.
     */
    private static @NonNull SourceOption getOption(String id) {
        if (id == null) {
            id = DaggerUtils.getComponent(getApplication()).settingsProvider().getUnprotectedSettings().getString(KEY_BASEMAP_SOURCE);
        }
        for (SourceOption option : sourceOptions) {
            if (option.id.equals(id)) {
                return option;
            }
        }

        return sourceOptions[0];
    }

    private static Collect getApplication() {
        return Collect.getInstance();
    }

    private static class SourceOption {
        private final String id;  // preference value to store
        private final int labelId;  // string resource ID
        private final MapConfigurator cftor;

        private SourceOption(String id, int labelId, MapConfigurator cftor) {
            this.id = id;
            this.labelId = labelId;
            this.cftor = cftor;
        }
    }
}
