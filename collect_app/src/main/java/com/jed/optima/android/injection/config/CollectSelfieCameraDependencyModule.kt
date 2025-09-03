package com.jed.optima.android.injection.config

import com.jed.optima.permissions.PermissionsChecker
import com.jed.optima.selfiecamera.SelfieCameraDependencyModule

class CollectSelfieCameraDependencyModule(
    private val appDependencyComponent: com.jed.optima.android.injection.config.AppDependencyComponent
) : SelfieCameraDependencyModule() {
    override fun providesPermissionChecker(): PermissionsChecker {
        return appDependencyComponent.permissionsChecker()
    }
}
