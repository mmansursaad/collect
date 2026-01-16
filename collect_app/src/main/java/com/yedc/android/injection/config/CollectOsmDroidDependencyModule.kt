package com.yedc.android.injection.config

import com.yedc.maps.MapConfigurator
import com.yedc.maps.layers.ReferenceLayerRepository
import com.yedc.osmdroid.OsmDroidDependencyModule
import com.yedc.settings.SettingsProvider
import com.yedc.settings.keys.ProjectKeys

class CollectOsmDroidDependencyModule(
    private val appDependencyComponent: _root_ide_package_.com.yedc.android.injection.config.AppDependencyComponent
) : OsmDroidDependencyModule() {
    override fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        return appDependencyComponent.referenceLayerRepository()
    }

    override fun providesLocationClient(): com.yedc.location.LocationClient {
        return appDependencyComponent.locationClient()
    }

    override fun providesMapConfigurator(): MapConfigurator {
        return _root_ide_package_.com.yedc.android.geo.MapConfiguratorProvider.getConfigurator(
            appDependencyComponent.settingsProvider().getUnprotectedSettings().getString(ProjectKeys.KEY_BASEMAP_SOURCE)
        )
    }

    override fun providesSettingsProvider(): SettingsProvider {
        return appDependencyComponent.settingsProvider()
    }
}
