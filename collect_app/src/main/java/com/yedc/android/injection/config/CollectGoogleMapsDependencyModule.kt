package com.yedc.android.injection.config

import com.yedc.googlemaps.GoogleMapsDependencyModule
import com.yedc.maps.layers.ReferenceLayerRepository
import com.yedc.settings.SettingsProvider

class CollectGoogleMapsDependencyModule(
    private val appDependencyComponent: _root_ide_package_.com.yedc.android.injection.config.AppDependencyComponent
) : GoogleMapsDependencyModule() {
    override fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        return appDependencyComponent.referenceLayerRepository()
    }

    override fun providesLocationClient(): com.yedc.location.LocationClient {
        return appDependencyComponent.locationClient()
    }

    override fun providesSettingsProvider(): SettingsProvider {
        return appDependencyComponent.settingsProvider()
    }
}
