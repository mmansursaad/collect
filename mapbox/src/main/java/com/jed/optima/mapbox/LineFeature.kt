package com.yedc.mapbox

import com.yedc.maps.MapPoint

interface LineFeature : MapFeature {
    val points: List<MapPoint>
}
