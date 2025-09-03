package com.jed.optima.mapbox

import com.jed.optima.maps.MapPoint

interface LineFeature : MapFeature {
    val points: List<MapPoint>
}
