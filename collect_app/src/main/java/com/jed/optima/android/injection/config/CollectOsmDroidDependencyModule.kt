package com.jed.optima.android.injection.config

import com.jed.optima.maps.MapConfigurator
import com.jed.optima.maps.layers.ReferenceLayerRepository
import com.jed.optima.osmdroid.OsmDroidDependencyModule
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.settings.keys.ProjectKeys

class CollectOsmDroidDependencyModule(
    private val appDependencyComponent: com.jed.optima.android.injection.config.AppDependencyComponent
) : OsmDroidDependencyModule() {
    override fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        return appDependencyComponent.referenceLayerRepository()
    }

    override fun providesLocationClient(): com.jed.optima.location.LocationClient {
        return appDependencyComponent.locationClient()
    }

    override fun providesMapConfigurator(): MapConfigurator {
        return com.jed.optima.android.geo.MapConfiguratorProvider.getConfigurator(
            appDependencyComponent.settingsProvider().getUnprotectedSettings().getString(ProjectKeys.KEY_BASEMAP_SOURCE)
        )
    }

    override fun providesSettingsProvider(): SettingsProvider {
        return appDependencyComponent.settingsProvider()
    }
}
