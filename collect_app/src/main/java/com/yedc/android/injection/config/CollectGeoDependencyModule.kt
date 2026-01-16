package com.yedc.android.injection.config

import android.app.Application
import android.content.Context
import android.location.LocationManager
import com.yedc.async.Scheduler
import com.yedc.geo.GeoDependencyModule
import com.yedc.location.satellites.GpsStatusSatelliteInfoClient
import com.yedc.location.satellites.SatelliteInfoClient
import com.yedc.location.tracker.ForegroundServiceLocationTracker
import com.yedc.location.tracker.LocationTracker
import com.yedc.maps.MapFragmentFactory
import com.yedc.maps.layers.ReferenceLayerRepository
import com.yedc.permissions.PermissionsChecker
import com.yedc.settings.SettingsProvider

class CollectGeoDependencyModule(
    private val appDependencyComponent: _root_ide_package_.com.yedc.android.injection.config.AppDependencyComponent
) : GeoDependencyModule() {

    override fun providesMapFragmentFactory(): MapFragmentFactory {
        return appDependencyComponent.mapFragmentFactory()
    }

    override fun providesLocationTracker(application: Application): LocationTracker {
        return ForegroundServiceLocationTracker(application)
    }

    override fun providesLocationClient(): com.yedc.location.LocationClient {
        return appDependencyComponent.locationClient()
    }

    override fun providesScheduler(): Scheduler {
        return appDependencyComponent.scheduler()
    }

    override fun providesSatelliteInfoClient(context: Context): SatelliteInfoClient {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return GpsStatusSatelliteInfoClient(locationManager)
    }

    override fun providesPermissionChecker(context: Context): PermissionsChecker {
        return appDependencyComponent.permissionsChecker()
    }

    override fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        return appDependencyComponent.referenceLayerRepository()
    }

    override fun providesSettingsProvider(): SettingsProvider {
        return appDependencyComponent.settingsProvider()
    }

    override fun providesExternalWebPageHelper(): com.yedc.webpage.ExternalWebPageHelper {
        return appDependencyComponent.externalWebPageHelper()
    }
}
