package com.yedc.android.injection.config

import com.yedc.permissions.PermissionsChecker
import com.yedc.selfiecamera.SelfieCameraDependencyModule

class CollectSelfieCameraDependencyModule(
    private val appDependencyComponent: _root_ide_package_.com.yedc.android.injection.config.AppDependencyComponent
) : SelfieCameraDependencyModule() {
    override fun providesPermissionChecker(): PermissionsChecker {
        return appDependencyComponent.permissionsChecker()
    }
}
