package com.jed.optima.android.geo

import com.jed.optima.android.application.MapboxClassInstanceCreator
import com.jed.optima.maps.MapFragment
import com.jed.optima.maps.MapFragmentFactory
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.settings.keys.ProjectKeys.BASEMAP_SOURCE_CARTO
import com.jed.optima.settings.keys.ProjectKeys.BASEMAP_SOURCE_MAPBOX
import com.jed.optima.settings.keys.ProjectKeys.BASEMAP_SOURCE_OSM
import com.jed.optima.settings.keys.ProjectKeys.BASEMAP_SOURCE_USGS
import com.jed.optima.settings.keys.ProjectKeys.KEY_BASEMAP_SOURCE

class MapFragmentFactoryImpl(private val settingsProvider: SettingsProvider) : MapFragmentFactory {

    override fun createMapFragment(): MapFragment {
        val settings = settingsProvider.getUnprotectedSettings()

        return when {
            isBasemapOSM(settings.getString(KEY_BASEMAP_SOURCE)) -> com.jed.optima.osmdroid.OsmDroidMapFragment()
            settings.getString(KEY_BASEMAP_SOURCE) == BASEMAP_SOURCE_MAPBOX -> MapboxClassInstanceCreator.createMapboxMapFragment()
            else -> com.jed.optima.googlemaps.GoogleMapFragment()
        }
    }

    private fun isBasemapOSM(basemap: String?): Boolean {
        return basemap == BASEMAP_SOURCE_OSM ||
            basemap == BASEMAP_SOURCE_USGS ||
            basemap == BASEMAP_SOURCE_CARTO
    }
}
