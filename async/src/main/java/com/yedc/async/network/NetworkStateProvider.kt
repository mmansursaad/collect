package com.yedc.async.network

import com.yedc.async.Scheduler

interface NetworkStateProvider {
    val currentNetwork: Scheduler.NetworkType?

    val isDeviceOnline: Boolean
        get() {
            return currentNetwork != null
        }
}
