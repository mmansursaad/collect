package com.yedc.android.support

import com.yedc.async.Scheduler
import com.yedc.async.network.NetworkStateProvider

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
