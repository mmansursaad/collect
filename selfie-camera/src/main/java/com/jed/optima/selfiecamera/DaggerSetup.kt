package com.jed.optima.selfiecamera

import dagger.Component
import dagger.Module
import dagger.Provides
import com.jed.optima.permissions.PermissionsChecker
import javax.inject.Singleton

interface SelfieCameraDependencyComponentProvider {
    val selfieCameraDependencyComponent: SelfieCameraDependencyComponent
}

@Component(modules = [SelfieCameraDependencyModule::class])
@Singleton
interface SelfieCameraDependencyComponent {
    fun inject(captureSelfieActivity: CaptureSelfieActivity)
}

@Module
open class SelfieCameraDependencyModule {

    @Provides
    open fun providesPermissionChecker(): PermissionsChecker {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    internal open fun providesCamera(): Camera {
        return CameraXCamera()
    }
}
