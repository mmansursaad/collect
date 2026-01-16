package com.yedc.location

import android.app.Application
import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import java.util.function.Supplier

/**
 * A static helper class for obtaining the appropriate LocationClient to use.
 */
object LocationClientProvider {

    private var testClient: _root_ide_package_.com.yedc.location.LocationClient? = null

    @JvmStatic
    fun getClient(application: Application): _root_ide_package_.com.yedc.location.LocationClient {
        return getClient(
            application,
            { GoogleFusedLocationClient(application) },
            GoogleApiAvailability.getInstance()
        )
    }

    /**
     * Returns a LocationClient appropriate for a given context.
     */
    // NOTE(ping): As of 2018-11-01, the GoogleFusedLocationClient never returns an
    // accuracy radius below 3m: https://issuetracker.google.com/issues/118789585
    internal fun getClient(
        context: Context,
        googleFusedLocationClientProvider: Supplier<GoogleFusedLocationClient>,
        googleApiAvailability: GoogleApiAvailability
    ): _root_ide_package_.com.yedc.location.LocationClient {
        val playServicesAvailable = googleApiAvailability
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

        return testClient.let {
            if (it != null) {
                it
            } else if (playServicesAvailable) {
                googleFusedLocationClientProvider.get()
            } else {
                _root_ide_package_.com.yedc.location.AndroidLocationClient(context)
            }
        }
    }

    /**
     * Sets the LocationClient.  For use in tests only.
     */
    fun setTestClient(testClient: _root_ide_package_.com.yedc.location.LocationClient?) {
        LocationClientProvider.testClient = testClient
    }
}
