package com.yedc.googlemaps

import dagger.Component
import dagger.Module
import dagger.Provides
import com.yedc.maps.layers.ReferenceLayerRepository
import com.yedc.settings.SettingsProvider
import javax.inject.Singleton

interface GoogleMapsDependencyComponentProvider {
    val googleMapsDependencyComponent: GoogleMapsDependencyComponent
}

@Component(modules = [GoogleMapsDependencyModule::class])
@Singleton
interface GoogleMapsDependencyComponent {
    fun inject(osmDroidMapFragment: _root_ide_package_.com.yedc.googlemaps.GoogleMapFragment)
}

@Module
open class GoogleMapsDependencyModule {

    @Provides
    open fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesLocationClient(): _root_ide_package_.com.yedc.location.LocationClient {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesSettingsProvider(): SettingsProvider {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }
}
