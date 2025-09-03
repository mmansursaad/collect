package com.jed.optima.geo

import android.Manifest
import android.app.Activity
import com.jed.optima.androidshared.ui.ToastUtils
import com.jed.optima.permissions.ContextCompatPermissionChecker

internal object GeoActivityUtils {

    @JvmStatic
    fun requireLocationPermissions(activity: Activity) {
        val permissionGranted = ContextCompatPermissionChecker(activity).isPermissionGranted(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (!permissionGranted) {
            ToastUtils.showLongToast(com.jed.optima.strings.R.string.not_granted_permission)
            activity.finish()
        }
    }
}
