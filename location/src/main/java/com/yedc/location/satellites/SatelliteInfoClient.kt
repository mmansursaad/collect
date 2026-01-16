package com.yedc.location.satellites

import com.yedc.androidshared.livedata.NonNullLiveData

interface SatelliteInfoClient {

    val satellitesUsedInLastFix: NonNullLiveData<Int>
}
