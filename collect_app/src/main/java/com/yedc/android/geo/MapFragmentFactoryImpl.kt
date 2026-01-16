package com.yedc.android.geo

import com.yedc.android.application.MapboxClassInstanceCreator
import com.yedc.maps.MapFragment
import com.yedc.maps.MapFragmentFactory
import com.yedc.settings.SettingsProvider
import com.yedc.settings.keys.ProjectKeys.BASEMAP_SOURCE_CARTO
import com.yedc.settings.keys.ProjectKeys.BASEMAP_SOURCE_MAPBOX
import com.yedc.settings.keys.ProjectKeys.BASEMAP_SOURCE_OSM
import com.yedc.settings.keys.ProjectKeys.BASEMAP_SOURCE_USGS
import com.yedc.settings.keys.ProjectKeys.KEY_BASEMAP_SOURCE

class MapFragmentFactoryImpl(private val settingsProvider: SettingsProvider) : MapFragmentFactory {

    override fun createMapFragment(): MapFragment {
        val settings = settingsProvider.getUnprotectedSettings()

        return when {
            isBasemapOSM(settings.getString(KEY_BASEMAP_SOURCE)) -> com.yedc.osmdroid.OsmDroidMapFragment()
            settings.getString(KEY_BASEMAP_SOURCE) == BASEMAP_SOURCE_MAPBOX -> MapboxClassInstanceCreator.createMapboxMapFragment()
            else -> com.yedc.googlemaps.GoogleMapFragment()
        }
    }

    private fun isBasemapOSM(basemap: String?): Boolean {
        return basemap == BASEMAP_SOURCE_OSM ||
            basemap == BASEMAP_SOURCE_USGS ||
            basemap == BASEMAP_SOURCE_CARTO
    }
}
