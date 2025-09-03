package com.jed.optima.location.satellites

import com.jed.optima.androidshared.livedata.NonNullLiveData

interface SatelliteInfoClient {

    val satellitesUsedInLastFix: NonNullLiveData<Int>
}
