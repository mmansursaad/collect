package com.jed.optima.async.network

import com.jed.optima.async.Scheduler

interface NetworkStateProvider {
    val currentNetwork: Scheduler.NetworkType?

    val isDeviceOnline: Boolean
        get() {
            return currentNetwork != null
        }
}
