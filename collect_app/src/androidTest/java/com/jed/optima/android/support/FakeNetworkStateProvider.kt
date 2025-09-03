package com.jed.optima.android.support

import com.jed.optima.async.Scheduler
import com.jed.optima.async.network.NetworkStateProvider

class FakeNetworkStateProvider : NetworkStateProvider {

    private var type: Scheduler.NetworkType? = Scheduler.NetworkType.WIFI

    fun goOnline(networkType: Scheduler.NetworkType) {
        type = networkType
    }

    fun goOffline() {
        type = null
    }

    override val currentNetwork: Scheduler.NetworkType?
        get() = type
}
