package com.jed.optima.osmdroid

import dagger.Component
import dagger.Module
import dagger.Provides
import com.jed.optima.maps.MapConfigurator
import com.jed.optima.maps.layers.ReferenceLayerRepository
import com.jed.optima.settings.SettingsProvider
import javax.inject.Singleton

interface OsmDroidDependencyComponentProvider {
    val osmDroidDependencyComponent: OsmDroidDependencyComponent
}

@Component(modules = [OsmDroidDependencyModule::class])
@Singleton
interface OsmDroidDependencyComponent {
    fun inject(osmDroidMapFragment: _root_ide_package_.com.jed.optima.osmdroid.OsmDroidMapFragment)
}

@Module
open class OsmDroidDependencyModule {

    @Provides
    open fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesLocationClient(): _root_ide_package_.com.jed.optima.location.LocationClient {
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
