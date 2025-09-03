package com.jed.optima.geo

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import com.jed.optima.async.Scheduler
import com.jed.optima.geo.geopoint.GeoPointActivity
import com.jed.optima.geo.geopoint.GeoPointDialogFragment
import com.jed.optima.geo.geopoint.GeoPointViewModelFactory
import com.jed.optima.geo.geopoint.LocationTrackerGeoPointViewModel
import com.jed.optima.geo.selection.SelectionMapFragment
import com.jed.optima.location.satellites.SatelliteInfoClient
import com.jed.optima.location.tracker.LocationTracker
import com.jed.optima.maps.MapFragmentFactory
import com.jed.optima.maps.layers.ReferenceLayerRepository
import com.jed.optima.permissions.PermissionsChecker
import com.jed.optima.settings.SettingsProvider
import javax.inject.Singleton

interface GeoDependencyComponentProvider {
    val geoDependencyComponent: GeoDependencyComponent
}

@Component(modules = [GeoDependencyModule::class])
@Singleton
interface GeoDependencyComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun geoDependencyModule(geoDependencyModule: GeoDependencyModule): Builder

        fun build(): GeoDependencyComponent
    }

    fun inject(geoPointMapActivity: _root_ide_package_.com.jed.optima.geo.geopoint.GeoPointMapActivity)
    fun inject(geoPolyActivity: _root_ide_package_.com.jed.optima.geo.geopoly.GeoPolyActivity)
    fun inject(geoPointDialogFragment: GeoPointDialogFragment)
    fun inject(geoPointActivity: GeoPointActivity)
    fun inject(selectionMapFragment: SelectionMapFragment)

    val scheduler: Scheduler
    val locationTracker: LocationTracker
    val satelliteInfoClient: SatelliteInfoClient
}

@Module
open class GeoDependencyModule {

    @Provides
    open fun context(application: Application): Context {
        return application
    }

    @Provides
    open fun providesMapFragmentFactory(): MapFragmentFactory {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesLocationTracker(application: Application): LocationTracker {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesLocationClient(): _root_ide_package_.com.jed.optima.location.LocationClient {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesScheduler(): Scheduler {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesSatelliteInfoClient(context: Context): SatelliteInfoClient {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesPermissionChecker(context: Context): PermissionsChecker {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    internal open fun providesGeoPointViewModelFactory(application: Application): GeoPointViewModelFactory {
        return object : GeoPointViewModelFactory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val componentProvider = application as GeoDependencyComponentProvider
                val component = componentProvider.geoDependencyComponent
                return LocationTrackerGeoPointViewModel(
                    component.locationTracker,
                    component.satelliteInfoClient,
                    System::currentTimeMillis,
                    component.scheduler
                ) as T
            }
        }
    }

    @Provides
    open fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesSettingsProvider(): SettingsProvider {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesExternalWebPageHelper(): _root_ide_package_.com.jed.optima.webpage.ExternalWebPageHelper {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }
}
