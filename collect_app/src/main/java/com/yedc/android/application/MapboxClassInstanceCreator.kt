package com.yedc.android.application

import androidx.fragment.app.Fragment
import com.yedc.maps.MapConfigurator
import com.yedc.maps.MapFragment

object MapboxClassInstanceCreator {

    private const val MAP_FRAGMENT = "com.yedc.mapbox.MapboxMapFragment"

    @JvmStatic
    fun isMapboxAvailable(): Boolean {
        return try {
            getClass(MAP_FRAGMENT)
            System.loadLibrary("mapbox-common")
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun createMapboxMapFragment(): MapFragment {
        return createClassInstance(MAP_FRAGMENT)
    }

    @JvmStatic
    fun createMapBoxInitializationFragment(): Fragment {
        return createClassInstance("com.yedc.mapbox.MapBoxInitializationFragment")
    }

    @JvmStatic
    fun createMapboxMapConfigurator(): MapConfigurator {
        return createClassInstance("com.yedc.mapbox.MapboxMapConfigurator")
    }

    private fun <T> createClassInstance(className: String): T {
        return getClass(className).newInstance() as T
    }

    private fun getClass(className: String): Class<*> = Class.forName(className)
}
