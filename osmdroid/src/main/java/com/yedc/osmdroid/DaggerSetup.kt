package com.yedc.osmdroid

import dagger.Component
import dagger.Module
import dagger.Provides
import com.yedc.maps.MapConfigurator
import com.yedc.maps.layers.ReferenceLayerRepository
import com.yedc.settings.SettingsProvider
import javax.inject.Singleton

interface OsmDroidDependencyComponentProvider {
    val osmDroidDependencyComponent: OsmDroidDependencyComponent
}

@Component(modules = [OsmDroidDependencyModule::class])
@Singleton
interface OsmDroidDependencyComponent {
    fun inject(osmDroidMapFragment: _root_ide_package_.com.yedc.osmdroid.OsmDroidMapFragment)
}

@Module
open class OsmDroidDependencyModule {

    @Provides
    open fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesLocationClient(): _root_ide_package_.com.yedc.location.LocationClient {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesMapConfigurator(): MapConfigurator {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesSettingsProvider(): SettingsProvider {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }
}
