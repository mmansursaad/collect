package com.jed.optima.android.injection.config

import com.jed.optima.googlemaps.GoogleMapsDependencyModule
import com.jed.optima.maps.layers.ReferenceLayerRepository
import com.jed.optima.settings.SettingsProvider

class CollectGoogleMapsDependencyModule(
    private val appDependencyComponent: com.jed.optima.android.injection.config.AppDependencyComponent
) : GoogleMapsDependencyModule() {
    override fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        return appDependencyComponent.referenceLayerRepository()
    }

    override fun providesLocationClient(): com.jed.optima.location.LocationClient {
        return appDependencyComponent.locationClient()
    }

    override fun providesSettingsProvider(): SettingsProvider {
        return appDependencyComponent.settingsProvider()
    }
}
