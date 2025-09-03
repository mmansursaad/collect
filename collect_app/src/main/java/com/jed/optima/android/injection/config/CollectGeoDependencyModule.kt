package com.jed.optima.android.injection.config

import android.app.Application
import android.content.Context
import android.location.LocationManager
import com.jed.optima.async.Scheduler
import com.jed.optima.geo.GeoDependencyModule
import com.jed.optima.location.satellites.GpsStatusSatelliteInfoClient
import com.jed.optima.location.satellites.SatelliteInfoClient
import com.jed.optima.location.tracker.ForegroundServiceLocationTracker
import com.jed.optima.location.tracker.LocationTracker
import com.jed.optima.maps.MapFragmentFactory
import com.jed.optima.maps.layers.ReferenceLayerRepository
import com.jed.optima.permissions.PermissionsChecker
import com.jed.optima.settings.SettingsProvider

class CollectGeoDependencyModule(
    private val appDependencyComponent: com.jed.optima.android.injection.config.AppDependencyComponent
) : GeoDependencyModule() {

    override fun providesMapFragmentFactory(): MapFragmentFactory {
        return appDependencyComponent.mapFragmentFactory()
    }

    override fun providesLocationTracker(application: Application): LocationTracker {
        return ForegroundServiceLocationTracker(application)
    }

    override fun providesLocationClient(): com.jed.optima.location.LocationClient {
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

    override fun providesExternalWebPageHelper(): com.jed.optima.webpage.ExternalWebPageHelper {
        return appDependencyComponent.externalWebPageHelper()
    }
}
