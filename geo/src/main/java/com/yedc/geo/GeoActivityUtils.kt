package com.yedc.geo

import android.Manifest
import android.app.Activity
import com.yedc.androidshared.ui.ToastUtils
import com.yedc.permissions.ContextCompatPermissionChecker

internal object GeoActivityUtils {

    @JvmStatic
    fun requireLocationPermissions(activity: Activity) {
        val permissionGranted = ContextCompatPermissionChecker(activity).isPermissionGranted(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (!permissionGranted) {
            ToastUtils.showLongToast(com.yedc.strings.R.string.not_granted_permission)
            activity.finish()
        }
    }
}
